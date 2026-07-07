package bg.tu_sofia.carpooling.bookings.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BookingRequest(
        @NotNull UUID ridePublicId,
        @Min(1) @Max(8) int seats,
        @Size(max = 500) String message,
        String turnstileToken
) {}
