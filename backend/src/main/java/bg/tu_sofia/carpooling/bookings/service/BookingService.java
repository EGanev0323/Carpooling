package bg.tu_sofia.carpooling.bookings.service;

import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.auth.repository.UserRepository;
import bg.tu_sofia.carpooling.auth.service.AuditService;
import bg.tu_sofia.carpooling.bookings.api.dto.BookingRequest;
import bg.tu_sofia.carpooling.bookings.api.dto.BookingResponse;
import bg.tu_sofia.carpooling.bookings.api.dto.BookingResponse.CitySummary;
import bg.tu_sofia.carpooling.bookings.api.dto.BookingResponse.PassengerSummary;
import bg.tu_sofia.carpooling.bookings.api.dto.BookingResponse.RideSummary;
import bg.tu_sofia.carpooling.bookings.domain.Booking;
import bg.tu_sofia.carpooling.bookings.repository.BookingRepository;
import bg.tu_sofia.carpooling.common.exception.BusinessException;
import bg.tu_sofia.carpooling.common.exception.ResourceNotFoundException;
import bg.tu_sofia.carpooling.common.exception.UnauthorizedException;
import bg.tu_sofia.carpooling.geo.domain.City;
import bg.tu_sofia.carpooling.rides.domain.Ride;
import bg.tu_sofia.carpooling.rides.repository.RideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {

    private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "CONFIRMED");

    private final BookingRepository bookingRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public BookingService(BookingRepository bookingRepository,
                          RideRepository rideRepository,
                          UserRepository userRepository,
                          AuditService auditService) {
        this.bookingRepository = bookingRepository;
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public BookingResponse createBooking(Long passengerId, BookingRequest request) {
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ride ride = rideRepository.findByPublicId(request.ridePublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + request.ridePublicId()));

        if (ride.getDriver().getId().equals(passengerId)) {
            throw new BusinessException("Driver cannot book their own ride", 422);
        }

        boolean alreadyBooked = bookingRepository.existsByRideIdAndPassengerIdAndStatusIn(
                ride.getId(), passengerId, ACTIVE_STATUSES);
        if (alreadyBooked) {
            throw new BusinessException("You already have an active booking for this ride", 409);
        }

        // Pessimistic lock to prevent race conditions on seat count
        Ride lockedRide = rideRepository.findByIdForUpdate(ride.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        if (!"ACTIVE".equals(lockedRide.getStatus())) {
            throw new BusinessException("Ride is not available for booking", 409);
        }

        if (!lockedRide.getDepartureAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessException("Ride has already departed", 409);
        }

        int booked = bookingRepository.sumBookedSeats(lockedRide.getId());
        int available = lockedRide.getTotalSeats() - booked;

        if (request.seats() > available) {
            throw new BusinessException("Not enough seats. Available: " + available, 409);
        }

        Booking booking = Booking.builder()
                .ride(lockedRide)
                .passenger(passenger)
                .seats((short) request.seats())
                .status("PENDING")
                .message(request.message())
                .build();

        Booking saved = bookingRepository.save(booking);

        auditService.log(passengerId, "BOOKING_CREATED", "BOOKING", saved.getId(),
                Map.of(
                        "rideId", lockedRide.getId(),
                        "seats", request.seats()
                ));

        return toResponse(saved);
    }

    @Transactional
    public BookingResponse confirmBooking(UUID publicId, Long driverId) {
        Booking booking = bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + publicId));

        if (!booking.getRide().getDriver().getId().equals(driverId)) {
            throw new UnauthorizedException("Only the ride driver can confirm bookings");
        }

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BusinessException("Only PENDING bookings can be confirmed", 409);
        }

        booking.setStatus("CONFIRMED");
        Booking saved = bookingRepository.save(booking);

        auditService.log(driverId, "BOOKING_CONFIRMED", "BOOKING", saved.getId(),
                Map.of("passengerId", booking.getPassenger().getId()));

        return toResponse(saved);
    }

    @Transactional
    public BookingResponse rejectBooking(UUID publicId, Long driverId) {
        Booking booking = bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + publicId));

        if (!booking.getRide().getDriver().getId().equals(driverId)) {
            throw new UnauthorizedException("Only the ride driver can reject bookings");
        }

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BusinessException("Only PENDING bookings can be rejected", 409);
        }

        booking.setStatus("REJECTED");
        Booking saved = bookingRepository.save(booking);

        auditService.log(driverId, "BOOKING_REJECTED", "BOOKING", saved.getId(),
                Map.of("passengerId", booking.getPassenger().getId()));

        return toResponse(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID publicId, Long passengerId) {
        Booking booking = bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + publicId));

        if (!booking.getPassenger().getId().equals(passengerId)) {
            throw new UnauthorizedException("Only the passenger can cancel their booking");
        }

        if (!ACTIVE_STATUSES.contains(booking.getStatus())) {
            throw new BusinessException("Only PENDING or CONFIRMED bookings can be cancelled", 409);
        }

        booking.setStatus("CANCELLED");
        Booking saved = bookingRepository.save(booking);

        auditService.log(passengerId, "BOOKING_CANCELLED", "BOOKING", saved.getId(),
                Map.of("rideId", booking.getRide().getId()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Long passengerId) {
        return bookingRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getRideBookings(UUID ridePublicId, Long driverId) {
        Ride ride = rideRepository.findByPublicId(ridePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + ridePublicId));

        if (!ride.getDriver().getId().equals(driverId)) {
            throw new UnauthorizedException("Only the ride driver can view all bookings");
        }

        return bookingRepository.findByRideIdAndStatusIn(ride.getId(),
                        List.of("PENDING", "CONFIRMED", "REJECTED", "CANCELLED", "NO_SHOW"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BookingResponse toResponse(Booking booking) {
        User passenger = booking.getPassenger();
        Ride ride = booking.getRide();

        PassengerSummary passengerSummary = new PassengerSummary(
                passenger.getPublicId(),
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getAvatarUrl()
        );

        CitySummary origin = toCitySummary(ride.getOriginCity());
        CitySummary destination = toCitySummary(ride.getDestinationCity());

        User driver = ride.getDriver();
        String driverName = driver.getFirstName() + " " + driver.getLastName();

        RideSummary rideSummary = new RideSummary(
                ride.getPublicId(),
                origin,
                destination,
                ride.getDepartureAt(),
                ride.getPricePerSeat(),
                driverName
        );

        return new BookingResponse(
                booking.getPublicId(),
                ride.getPublicId(),
                passengerSummary,
                booking.getSeats(),
                booking.getStatus(),
                booking.getMessage(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                rideSummary
        );
    }

    private CitySummary toCitySummary(City city) {
        return new CitySummary(
                city.getId(),
                city.getSlug(),
                city.getNameBg(),
                city.getNameEn()
        );
    }
}
