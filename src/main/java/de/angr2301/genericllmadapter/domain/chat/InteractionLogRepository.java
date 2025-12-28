package de.angr2301.genericllmadapter.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionLogRepository extends JpaRepository<InteractionLog, UUID> {
    List<InteractionLog> findBySessionIdOrderByTimestampAsc(UUID sessionId);
}
