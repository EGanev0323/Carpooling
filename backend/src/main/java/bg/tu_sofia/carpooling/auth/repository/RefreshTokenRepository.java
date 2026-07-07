package bg.tu_sofia.carpooling.auth.repository;

import bg.tu_sofia.carpooling.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    List<RefreshToken> findActiveByUserId(@Param("userId") Long userId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    void revokeAllByUserId(@Param("userId") Long userId, @Param("now") OffsetDateTime now);
}
