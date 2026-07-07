package bg.tu_sofia.carpooling.chat.service;

import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.auth.repository.UserRepository;
import bg.tu_sofia.carpooling.auth.service.AuditService;
import bg.tu_sofia.carpooling.bookings.repository.BookingRepository;
import bg.tu_sofia.carpooling.chat.api.dto.ChatChannelResponse;
import bg.tu_sofia.carpooling.chat.api.dto.ChatChannelResponse.ParticipantSummary;
import bg.tu_sofia.carpooling.chat.api.dto.ChatMessagePayload;
import bg.tu_sofia.carpooling.chat.api.dto.ChatMessageResponse;
import bg.tu_sofia.carpooling.chat.api.dto.ChatMessageResponse.SenderSummary;
import bg.tu_sofia.carpooling.chat.domain.ChatChannel;
import bg.tu_sofia.carpooling.chat.domain.ChatMessage;
import bg.tu_sofia.carpooling.chat.repository.ChatChannelRepository;
import bg.tu_sofia.carpooling.chat.repository.ChatMessageRepository;
import bg.tu_sofia.carpooling.common.exception.BusinessException;
import bg.tu_sofia.carpooling.common.exception.ResourceNotFoundException;
import bg.tu_sofia.carpooling.common.exception.UnauthorizedException;
import bg.tu_sofia.carpooling.rides.domain.Ride;
import bg.tu_sofia.carpooling.rides.repository.RideRepository;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final int MAX_MESSAGES_PER_MINUTE = 10;

    private final ChatChannelRepository channelRepository;
    private final ChatMessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final Cache<String, Bucket> bucketCache;

    public ChatService(ChatChannelRepository channelRepository,
                       ChatMessageRepository messageRepository,
                       BookingRepository bookingRepository,
                       RideRepository rideRepository,
                       UserRepository userRepository,
                       AuditService auditService,
                       Cache<String, Bucket> bucketCache) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.bookingRepository = bookingRepository;
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.bucketCache = bucketCache;
    }

    /**
     * Finds or creates a channel between driver and passenger for a CONFIRMED booking.
     * Called automatically from BookingService on confirm.
     */
    @Transactional
    public ChatChannel getOrCreateChannel(Long rideId, Long driverId, Long passengerId) {
        return channelRepository
                .findByRideIdAndDriverIdAndPassengerId(rideId, driverId, passengerId)
                .orElseGet(() -> {
                    boolean hasConfirmedBooking = bookingRepository
                            .existsByRideIdAndPassengerIdAndStatusIn(rideId, passengerId, List.of("CONFIRMED"));
                    if (!hasConfirmedBooking) {
                        throw new BusinessException(
                                "Cannot create chat channel: no confirmed booking exists for this ride/passenger combination",
                                422
                        );
                    }

                    Ride ride = rideRepository.findById(rideId)
                            .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));
                    User driver = userRepository.findById(driverId)
                            .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
                    User passenger = userRepository.findById(passengerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Passenger not found: " + passengerId));

                    ChatChannel channel = ChatChannel.builder()
                            .ride(ride)
                            .driver(driver)
                            .passenger(passenger)
                            .build();

                    ChatChannel saved = channelRepository.save(channel);
                    log.debug("Created chat channel id={} for ride={} driver={} passenger={}",
                            saved.getId(), rideId, driverId, passengerId);
                    return saved;
                });
    }

    /**
     * Sends a message. Called from the WebSocket controller.
     * Rate limit: 10 messages/minute/channel/user.
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, ChatMessagePayload payload) {
        ChatChannel channel = findAndValidateChannel(senderId, payload.channelId());
        checkRateLimit(senderId, payload.channelId());

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + senderId));

        ChatMessage message = ChatMessage.builder()
                .channel(channel)
                .sender(sender)
                .content(payload.content())
                .build();

        ChatMessage saved = messageRepository.save(message);

        auditService.log(senderId, "CHAT_MESSAGE_SENT", "CHAT_MESSAGE", saved.getId(),
                Map.of("channelId", payload.channelId()));

        return toMessageResponse(saved);
    }

    /**
     * Returns paginated message history for a channel.
     * Only a participant (driver or passenger) may access.
     */
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessageHistory(Long channelId, Long userId, Pageable pageable) {
        validateParticipant(channelId, userId);
        return messageRepository.findByChannelIdOrderByCreatedAtAsc(channelId, pageable)
                .map(this::toMessageResponse);
    }

    /**
     * Returns all channels the user participates in (as driver or passenger).
     */
    @Transactional(readOnly = true)
    public List<ChatChannelResponse> getMyChannels(Long userId) {
        return channelRepository.findByUserId(userId).stream()
                .map(channel -> toChannelResponse(channel, userId))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ChatChannel findAndValidateChannel(Long userId, Long channelId) {
        ChatChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat channel not found: " + channelId));

        boolean isParticipant = channel.getDriver().getId().equals(userId)
                || channel.getPassenger().getId().equals(userId);

        if (!isParticipant) {
            throw new UnauthorizedException("You are not a participant of this chat channel");
        }

        return channel;
    }

    private void validateParticipant(Long channelId, Long userId) {
        ChatChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat channel not found: " + channelId));

        boolean isParticipant = channel.getDriver().getId().equals(userId)
                || channel.getPassenger().getId().equals(userId);

        if (!isParticipant) {
            throw new UnauthorizedException("You are not a participant of this chat channel");
        }
    }

    private void checkRateLimit(Long senderId, Long channelId) {
        String key = "chat:" + senderId + ":" + channelId;
        Bucket bucket = bucketCache.get(key, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(MAX_MESSAGES_PER_MINUTE)
                                .refillGreedy(MAX_MESSAGES_PER_MINUTE, Duration.ofMinutes(1))
                                .build())
                        .build()
        );
        if (!bucket.tryConsume(1)) {
            throw new BusinessException("Rate limit exceeded. Max " + MAX_MESSAGES_PER_MINUTE + " messages/minute.", 429);
        }
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        User sender = message.getSender();
        SenderSummary senderSummary = new SenderSummary(
                sender.getId(),
                sender.getFirstName(),
                sender.getLastName(),
                sender.getAvatarUrl()
        );
        return new ChatMessageResponse(
                message.getId(),
                message.getChannel().getId(),
                senderSummary,
                message.getContent(),
                message.getCreatedAt(),
                message.getReadAt()
        );
    }

    private ChatChannelResponse toChannelResponse(ChatChannel channel, Long currentUserId) {
        Ride ride = channel.getRide();
        User driver = channel.getDriver();
        User passenger = channel.getPassenger();

        long unreadCount = messageRepository.countUnreadMessages(channel.getId(), currentUserId);

        ParticipantSummary driverSummary = new ParticipantSummary(
                driver.getId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getAvatarUrl()
        );
        ParticipantSummary passengerSummary = new ParticipantSummary(
                passenger.getId(),
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getAvatarUrl()
        );

        return new ChatChannelResponse(
                channel.getId(),
                ride.getPublicId(),
                ride.getOriginCity().getNameEn(),
                ride.getDestinationCity().getNameEn(),
                driverSummary,
                passengerSummary,
                channel.getCreatedAt(),
                unreadCount
        );
    }
}
