package com.euni.backend.entity;

import com.euni.backend.entity.enums.ProgramStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProgramStatus status = ProgramStatus.DRAFT;

    @Column(name = "general_objective", columnDefinition = "TEXT")
    private String generalObjective;

    @Column(name = "specific_objectives", columnDefinition = "TEXT")
    private String specificObjectives;

    @Column(name = "learning_outcomes", columnDefinition = "TEXT")
    private String learningOutcomes;

    @Column(name = "data", columnDefinition = "TEXT")
    private String data; // Synced metadata (PEO/PLO/PI JSON)

    @Column(name = "current_revision")
    private Integer currentRevision = 1;
}
