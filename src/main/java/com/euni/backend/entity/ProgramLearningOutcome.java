package com.euni.backend.entity;

import com.euni.backend.entity.enums.DomainType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "program_learning_outcomes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramLearningOutcome extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_learning_outcomes_id_seq")
    @SequenceGenerator(name = "program_learning_outcomes_id_seq", sequenceName = "program_learning_outcomes_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(nullable = false, length = 50)
    private String code;

    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private DomainType domain = DomainType.KNOWLEDGE;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "outcome", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PerformanceIndicator> indicators = new ArrayList<>();
}
