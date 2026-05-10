package com.euni.backend.entity.history;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "program_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    private String name;
    private String code;
    private String description;

    @Column(name = "general_objective", columnDefinition = "TEXT")
    private String generalObjective;

    @Column(name = "specific_objectives", columnDefinition = "TEXT")
    private String specificObjectives;

    @Column(name = "learning_outcomes", columnDefinition = "TEXT")
    private String learningOutcomes;

    @Column(name = "revision_number", nullable = false)
    private Integer revisionNumber;

    @CreationTimestamp
    @Column(name = "changed_at")
    private ZonedDateTime changedAt;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "change_reason")
    private String changeReason;
}
