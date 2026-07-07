package bg.tu_sofia.carpooling.bookings.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID publicId,
        UUID ridePublicId,
        PassengerSummary passenger,
        int seats,
        String status,
        String message,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        RideSummary ride
) {

    public record PassengerSummary(
            UUID publicId,
            String firstName,
            String lastName,
            String avatarUrl
    ) {}

    public record RideSummary(
            UUID publicId,
            CitySummary origin,
            CitySummary destination,
            OffsetDateTime departureAt,
            BigDecimal pricePerSeat,
            String driverName
    ) {}

    public record CitySummary(
            Integer id,
            String slug,
            String nameBg,
            String nameEn
    ) {}
}
