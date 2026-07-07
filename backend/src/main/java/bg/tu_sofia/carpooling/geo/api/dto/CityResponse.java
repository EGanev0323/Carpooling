package bg.tu_sofia.carpooling.geo.api.dto;

import java.math.BigDecimal;

public record CityResponse(
        Integer id,
        String slug,
        String nameBg,
        String nameEn,
        String regionBg,
        BigDecimal latitude,
        BigDecimal longitude
) {}
