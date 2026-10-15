package com.euni.backend.entity;

import com.euni.backend.entity.enums.ProgramStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "programs_id_seq")
    @SequenceGenerator(name = "programs_id_seq", sequenceName = "programs_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Program parent;

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
    @Builder.Default
    private ProgramStatus status = ProgramStatus.DRAFT;

    @Column(name = "effective_year")
    private Integer effectiveYear;

    @Column(name = "total_credits")
    private Integer totalCredits;

    @Column(name = "degree_level", length = 50)
    @Builder.Default
    private String degreeLevel = "ENGINEER";

    @Column(name = "training_mode", length = 50)
    @Builder.Default
    private String trainingMode = "FULL_TIME";

    @Column(name = "current_revision")
    @Builder.Default
    private Integer currentRevision = 1;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramStructure> structures = new ArrayList<>();

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramObjective> objectives = new ArrayList<>();

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramLearningOutcome> learningOutcomes = new ArrayList<>();
}
