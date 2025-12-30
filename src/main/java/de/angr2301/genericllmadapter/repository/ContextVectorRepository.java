package de.angr2301.genericllmadapter.repository;

import de.angr2301.genericllmadapter.domain.ContextVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * JPA Repository für ContextVector-Entitäten (pgVector-Integration).
 * Spring erkennt dieses Interface über @EnableJpaRepositories automatisch.
 */
public interface ContextVectorRepository extends JpaRepository<ContextVector, UUID> {

    // Native query for Cosine Similarity
    // <=> is cosine distance operator in pgvector. Order by distance ASC = most
    // similar.
    @Query(value = "SELECT * FROM operational_data.context_vectors " +
            "ORDER BY embedding <=> :embedding " +
            "LIMIT :limit", nativeQuery = true)
    List<ContextVector> findSimilar(@Param("embedding") float[] embedding, @Param("limit") int limit);
}
