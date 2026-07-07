package com.carpooling.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String avatarUrl,
        Boolean isDriver,
        Boolean isPassenger,
        Boolean isAdmin,
        BigDecimal avgRating,
        Integer ratingCount,
        Integer tripsAsDriver,
        Integer tripsAsPassenger,
        OffsetDateTime createdAt
) {}
