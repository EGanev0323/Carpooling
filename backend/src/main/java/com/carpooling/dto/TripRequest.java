package com.carpooling.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripRequest(
        @NotBlank @Size(max = 100) String originCity,
        @Size(max = 255) String originAddress,
        @NotBlank @Size(max = 100) String destinationCity,
        @Size(max = 255) String destinationAddress,
        @NotNull @Future LocalDateTime departureTime,
        LocalDateTime estimatedArrival,
        @NotNull @DecimalMin("0.0") BigDecimal pricePerSeat,
        @NotNull @Min(1) @Max(8) Integer totalSeats,
        @NotNull Long carId,
        String description,
        Boolean smokingAllowed,
        Boolean petsAllowed
) {}
