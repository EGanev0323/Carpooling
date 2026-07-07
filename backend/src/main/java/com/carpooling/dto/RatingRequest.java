package com.carpooling.dto;

import jakarta.validation.constraints.*;

public record RatingRequest(
        @NotNull Long ratedId,
        @NotNull @Min(1) @Max(5) Integer score,
        String comment
) {}
