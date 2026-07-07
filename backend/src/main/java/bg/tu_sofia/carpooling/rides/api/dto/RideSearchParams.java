package bg.tu_sofia.carpooling.rides.api.dto;

import java.time.LocalDate;

public record RideSearchParams(
        Integer originCityId,
        Integer destinationCityId,
        LocalDate date
) {}
