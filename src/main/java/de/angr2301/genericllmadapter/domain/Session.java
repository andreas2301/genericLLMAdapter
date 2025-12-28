package de.angr2301.genericllmadapter.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "sessions", schema = "operational_data")
public class Session {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "last_interaction_at")
    private OffsetDateTime lastInteractionAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @PrePersist
    public void prePersist() {
        if (startedAt == null) {
            startedAt = OffsetDateTime.now();
        }
        if (lastInteractionAt == null) {
            lastInteractionAt = OffsetDateTime.now();
        }
    }
}
