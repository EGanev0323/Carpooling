package com.carpooling.dto;

import com.carpooling.entity.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripSummaryResponse(
        Long id,
        String originCity,
        String destinationCity,
        LocalDateTime departureTime,
        BigDecimal pricePerSeat,
        Integer availableSeats,
        TripStatus status,
        DriverSummary driver
) {}
