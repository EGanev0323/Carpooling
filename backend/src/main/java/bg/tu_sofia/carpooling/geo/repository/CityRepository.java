package bg.tu_sofia.carpooling.geo.repository;

import bg.tu_sofia.carpooling.geo.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {

    List<City> findAllByIsActiveTrue();
}
