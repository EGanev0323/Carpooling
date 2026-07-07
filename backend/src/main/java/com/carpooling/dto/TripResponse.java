package com.carpooling.dto;

import com.carpooling.entity.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record TripResponse(
        Long id,
        String originCity,
        String originAddress,
        String destinationCity,
        String destinationAddress,
        LocalDateTime departureTime,
        LocalDateTime estimatedArrival,
        BigDecimal pricePerSeat,
        Integer totalSeats,
        Integer availableSeats,
        TripStatus status,
        String description,
        Boolean smokingAllowed,
        Boolean petsAllowed,
        OffsetDateTime createdAt,
        UserResponse driver,
        CarResponse car
) {}
