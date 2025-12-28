package de.angr.genericllmadapter.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
// import com.pgvector.PGvector; // If using explicit type mapping, but usually double[] or custom binder used
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "context_vectors", schema = "operational_data")
public class ContextVector {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "embedding", columnDefinition = "vector")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536) // Assuming OpenAI dim
    private float[] embedding; // Hibernate 6.4+ / pgvector-java support float[] directly often

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
