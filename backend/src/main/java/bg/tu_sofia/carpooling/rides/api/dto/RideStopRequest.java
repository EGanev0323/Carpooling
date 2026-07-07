package bg.tu_sofia.carpooling.rides.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record RideStopRequest(
        @NotNull Integer cityId,
        @NotNull int stopOrder,
        OffsetDateTime arriveAt
) {}
