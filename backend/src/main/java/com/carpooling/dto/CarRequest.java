package com.carpooling.dto;

import jakarta.validation.constraints.*;

public record CarRequest(
        @NotBlank @Size(max = 50) String make,
        @NotBlank @Size(max = 50) String model,
        @NotNull @Min(1900) @Max(2100) Integer year,
        @NotBlank @Size(max = 30) String color,
        @NotBlank @Size(max = 20) String licensePlate,
        @NotNull @Min(1) @Max(9) Integer totalSeats
) {}
