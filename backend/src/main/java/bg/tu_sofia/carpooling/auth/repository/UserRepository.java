package bg.tu_sofia.carpooling.auth.repository;

import bg.tu_sofia.carpooling.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    Optional<User> findActiveByEmail(@Param("email") String email);

    Optional<User> findByPublicId(UUID publicId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    boolean existsActiveByEmail(@Param("email") String email);
}
