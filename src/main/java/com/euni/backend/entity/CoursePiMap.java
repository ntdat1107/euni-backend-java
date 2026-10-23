package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "course_pi_maps", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"program_course_id", "pi_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePiMap extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_pi_maps_id_seq")
    @SequenceGenerator(name = "course_pi_maps_id_seq", sequenceName = "course_pi_maps_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_course_id", nullable = false)
    private ProgramCourse programCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pi_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PerformanceIndicator performanceIndicator;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1; // 1: Introductory, 2: Reinforce, 3: Mastery
}
