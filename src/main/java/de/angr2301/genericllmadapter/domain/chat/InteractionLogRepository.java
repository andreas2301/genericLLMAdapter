package de.angr2301.genericllmadapter.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JPA Repository für InteractionLog-Entitäten.
 * Spring erkennt dieses Interface über @EnableJpaRepositories automatisch.
 */
public interface InteractionLogRepository extends JpaRepository<InteractionLog, UUID> {
    List<InteractionLog> findBySessionIdOrderByTimestampAsc(UUID sessionId);
}
