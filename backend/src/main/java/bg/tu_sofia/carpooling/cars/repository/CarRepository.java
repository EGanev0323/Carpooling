package bg.tu_sofia.carpooling.cars.repository;

import bg.tu_sofia.carpooling.cars.domain.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    @Query("SELECT COUNT(r) FROM Ride r WHERE r.car.id = :carId AND r.status = 'ACTIVE'")
    long countActiveRidesByCar(@Param("carId") Long carId);
}
