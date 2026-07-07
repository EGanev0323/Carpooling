package com.carpooling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull @Min(1) Integer seatsBooked,
        String message
) {}
