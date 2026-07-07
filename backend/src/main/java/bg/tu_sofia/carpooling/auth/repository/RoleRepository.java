package bg.tu_sofia.carpooling.auth.repository;

import bg.tu_sofia.carpooling.auth.domain.Role;
import bg.tu_sofia.carpooling.auth.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Short> {

    Optional<Role> findByName(RoleName name);
}
