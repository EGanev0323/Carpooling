package bg.tu_sofia.carpooling.rides.repository;

import bg.tu_sofia.carpooling.rides.domain.Ride;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    Optional<Ride> findByPublicId(UUID publicId);

    @Query("""
            SELECT r FROM Ride r
            JOIN FETCH r.driver d
            JOIN FETCH r.car c
            JOIN FETCH r.originCity oc
            JOIN FETCH r.destinationCity dc
            WHERE r.status = 'ACTIVE'
              AND (:originCityId IS NULL OR r.originCity.id = :originCityId)
              AND (:destinationCityId IS NULL OR r.destinationCity.id = :destinationCityId)
              AND (:dateFrom IS NULL OR r.departureAt >= :dateFrom)
              AND (:dateTo IS NULL OR r.departureAt <= :dateTo)
              AND r.departureAt > CURRENT_TIMESTAMP
            ORDER BY r.departureAt ASC
            """)
    Page<Ride> search(
            @Param("originCityId") Integer originCityId,
            @Param("destinationCityId") Integer destinationCityId,
            @Param("dateFrom") OffsetDateTime dateFrom,
            @Param("dateTo") OffsetDateTime dateTo,
            Pageable pageable);

    List<Ride> findByDriverIdOrderByDepartureAtDesc(Long driverId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdForUpdate(@Param("id") Long id);
}
