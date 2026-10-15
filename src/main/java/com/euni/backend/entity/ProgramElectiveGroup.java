package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "program_elective_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramElectiveGroup extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_elective_groups_id_seq")
    @SequenceGenerator(name = "program_elective_groups_id_seq", sequenceName = "program_elective_groups_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id", nullable = false)
    private ProgramStructure structure;

    @Column(nullable = false)
    private String name;

    @Column(name = "min_credits")
    @Builder.Default
    private Integer minCredits = 0;

    @Column(name = "min_courses")
    @Builder.Default
    private Integer minCourses = 0;
}
