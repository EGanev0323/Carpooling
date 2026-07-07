package bg.tu_sofia.carpooling.cars.api.dto;

import jakarta.validation.constraints.*;

public record CarRequest(
        @NotBlank @Size(max = 64) String make,
        @NotBlank @Size(max = 64) String model,
        @Min(1990) @Max(2100) int year,
        @Size(max = 32) String color,
        @NotBlank @Size(max = 20) String licensePlate,
        @Min(2) @Max(8) int seats,
        String amenities
) {}
