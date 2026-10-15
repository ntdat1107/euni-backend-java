package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "objective_pi_maps", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"objective_id", "pi_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectivePiMap extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "objective_pi_maps_id_seq")
    @SequenceGenerator(name = "objective_pi_maps_id_seq", sequenceName = "objective_pi_maps_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objective_id", nullable = false)
    private ProgramObjective objective;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pi_id", nullable = false)
    private PerformanceIndicator performanceIndicator;

    private String note;
}
