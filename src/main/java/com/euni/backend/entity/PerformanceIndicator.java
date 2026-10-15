package com.euni.backend.entity;

import com.euni.backend.entity.enums.BloomLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "performance_indicators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceIndicator extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "performance_indicators_id_seq")
    @SequenceGenerator(name = "performance_indicators_id_seq", sequenceName = "performance_indicators_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outcome_id", nullable = false)
    private ProgramLearningOutcome outcome;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "bloom_level", length = 50)
    private BloomLevel bloomLevel;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;
}
