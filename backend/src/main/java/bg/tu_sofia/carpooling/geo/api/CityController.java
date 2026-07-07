package bg.tu_sofia.carpooling.geo.api;

import bg.tu_sofia.carpooling.geo.api.dto.CityResponse;
import bg.tu_sofia.carpooling.geo.domain.City;
import bg.tu_sofia.carpooling.geo.repository.CityRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController {

    private final CityRepository cityRepository;

    public CityController(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @GetMapping
    @Cacheable("cities")
    public ResponseEntity<List<CityResponse>> listActiveCities() {
        List<CityResponse> cities = cityRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(cities);
    }

    private CityResponse toResponse(City city) {
        return new CityResponse(
                city.getId(),
                city.getSlug(),
                city.getNameBg(),
                city.getNameEn(),
                city.getRegionBg(),
                city.getLatitude(),
                city.getLongitude()
        );
    }
}
