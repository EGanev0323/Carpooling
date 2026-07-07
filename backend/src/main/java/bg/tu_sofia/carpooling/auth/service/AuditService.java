package bg.tu_sofia.carpooling.auth.service;

import bg.tu_sofia.carpooling.auth.repository.AuditEventRepository;
import bg.tu_sofia.carpooling.auth.domain.AuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String eventType, String entityType, Long entityId, Map<String, Object> metadata) {
        try {
            String metadataJson = objectMapper.writeValueAsString(metadata);
            AuditEvent event = AuditEvent.builder()
                    .userId(userId)
                    .eventType(eventType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .metadata(metadataJson)
                    .createdAt(OffsetDateTime.now())
                    .build();
            auditEventRepository.save(event);
        } catch (Exception ex) {
            log.error("Failed to persist audit event [{}]: {}", eventType, ex.getMessage());
        }
    }
}
