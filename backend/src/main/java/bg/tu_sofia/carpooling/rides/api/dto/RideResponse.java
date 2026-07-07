package bg.tu_sofia.carpooling.rides.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RideResponse(
        UUID publicId,
        DriverSummary driver,
        CarSummary car,
        CitySummary originCity,
        CitySummary destinationCity,
        OffsetDateTime departureAt,
        OffsetDateTime arrivalAtEstimate,
        int totalSeats,
        int availableSeats,
        BigDecimal pricePerSeat,
        String routePolyline,
        String description,
        String status,
        OffsetDateTime createdAt,
        Double avgRating
) {

    public record DriverSummary(
            UUID publicId,
            String firstName,
            String lastName,
            String avatarUrl
    ) {}

    public record CarSummary(
            String make,
            String model,
            int year,
            String color,
            int seats,
            String amenities
    ) {}

    public record CitySummary(
            Integer id,
            String slug,
            String nameBg,
            String nameEn,
            BigDecimal latitude,
            BigDecimal longitude
    ) {}
}
