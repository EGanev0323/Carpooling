package bg.tu_sofia.carpooling.chat.repository;

import bg.tu_sofia.carpooling.chat.domain.ChatChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {

    Optional<ChatChannel> findByRideIdAndDriverIdAndPassengerId(Long rideId, Long driverId, Long passengerId);

    @Query("""
            SELECT c FROM ChatChannel c
            WHERE c.driver.id = :userId OR c.passenger.id = :userId
            ORDER BY c.createdAt DESC
            """)
    List<ChatChannel> findByUserId(@Param("userId") Long userId);
}
