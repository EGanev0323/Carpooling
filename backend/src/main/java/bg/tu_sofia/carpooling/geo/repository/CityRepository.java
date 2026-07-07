package bg.tu_sofia.carpooling.geo.repository;

import bg.tu_sofia.carpooling.geo.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {

    List<City> findAllByIsActiveTrue();

    Optional<City> findBySlug(String slug);
}
