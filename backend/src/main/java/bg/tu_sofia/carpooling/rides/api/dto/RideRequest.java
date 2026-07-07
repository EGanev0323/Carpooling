package bg.tu_sofia.carpooling.rides.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record RideRequest(
        @NotNull Integer originCityId,
        @NotNull Integer destinationCityId,
        @NotNull @Future OffsetDateTime departureAt,
        @NotNull Long carId,
        @Min(1) @Max(7) int totalSeats,
        @DecimalMin("0") @DecimalMax("200") BigDecimal pricePerSeat,
        @Size(max = 500) String description,
        List<RideStopRequest> stops
) {}
