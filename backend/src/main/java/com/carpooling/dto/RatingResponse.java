package com.carpooling.dto;

import java.time.OffsetDateTime;

public record RatingResponse(
        Long id,
        Long tripId,
        UserResponse rater,
        UserResponse rated,
        Integer score,
        String comment,
        OffsetDateTime createdAt
) {}
