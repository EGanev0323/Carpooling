package com.carpooling.dto;

import com.carpooling.entity.BookingStatus;

import java.time.OffsetDateTime;

public record BookingResponse(
        Long id,
        TripSummaryResponse trip,
        UserResponse passenger,
        Integer seatsBooked,
        BookingStatus status,
        String message,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
