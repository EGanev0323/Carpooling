package com.carpooling.dto;

import java.math.BigDecimal;

public record DriverSummary(
        Long id,
        String firstName,
        String lastName,
        BigDecimal avgRating
) {}
