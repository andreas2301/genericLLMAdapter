package de.angr2301.genericllmadapter.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ChatSession domain entity.
 * This is for chat conversation history, NOT HTTP login sessions (which are in
 * Redis).
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByUserIdOrderByStartedAtDesc(UUID userId);
}
