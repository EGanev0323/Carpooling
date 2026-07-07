package bg.tu_sofia.carpooling.cars.api.dto;

import java.time.OffsetDateTime;

public record CarResponse(
        Long id,
        String make,
        String model,
        int year,
        String color,
        String licensePlate,
        int seats,
        String amenities,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        boolean isOwner
) {}
