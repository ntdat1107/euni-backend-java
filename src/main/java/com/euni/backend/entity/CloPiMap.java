package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
@Table(name = "clo_pi_maps", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"clo_id", "pi_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloPiMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clo_pi_maps_id_seq")
    @SequenceGenerator(name = "clo_pi_maps_id_seq", sequenceName = "clo_pi_maps_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clo_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CourseLearningOutcome clo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pi_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PerformanceIndicator performanceIndicator;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weight = BigDecimal.valueOf(1.0);
}
