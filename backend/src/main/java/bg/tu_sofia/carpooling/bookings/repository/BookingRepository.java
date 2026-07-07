package bg.tu_sofia.carpooling.bookings.repository;

import bg.tu_sofia.carpooling.bookings.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPublicId(UUID publicId);

    List<Booking> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    List<Booking> findByRideIdAndStatusIn(Long rideId, List<String> statuses);

    @Query("SELECT COALESCE(SUM(b.seats), 0) FROM Booking b WHERE b.ride.id = :rideId AND b.status IN ('PENDING','CONFIRMED')")
    int sumBookedSeats(@Param("rideId") Long rideId);

    boolean existsByRideIdAndPassengerIdAndStatusIn(Long rideId, Long passengerId, List<String> statuses);

    @Modifying
    @Query("UPDATE Booking b SET b.status = 'CANCELLED', b.updatedAt = CURRENT_TIMESTAMP WHERE b.ride.id = :rideId AND b.status IN ('PENDING','CONFIRMED')")
    void cancelAllActiveByRideId(@Param("rideId") Long rideId);
}
