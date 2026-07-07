package bg.tu_sofia.carpooling.auth.repository;

import bg.tu_sofia.carpooling.auth.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
}
