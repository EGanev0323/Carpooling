package bg.tu_sofia.carpooling.chat.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatChannelResponse(
        Long id,
        UUID ridePublicId,
        String originCity,
        String destinationCity,
        ParticipantSummary driver,
        ParticipantSummary passenger,
        OffsetDateTime createdAt,
        Long unreadCount
) {
    public record ParticipantSummary(Long id, String firstName, String lastName, String avatarUrl) {}
}
