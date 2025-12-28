package de.angr2301.genericllmadapter.domain.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "interaction_logs", schema = "log_data")
public class InteractionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(nullable = false)
    private String role; // USER, ASSISTANT, SYSTEM

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "token_count")
    private Integer tokenCount;

    private String provider;

    @CreationTimestamp
    private LocalDateTime timestamp;
}
