package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "program_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramCourse extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_courses_id_seq")
    @SequenceGenerator(name = "program_courses_id_seq", sequenceName = "program_courses_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id")
    private ProgramStructure structure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elective_group_id")
    private ProgramElectiveGroup electiveGroup;

    private Integer semester;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean required = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_syllabus_id")
    private Syllabus appliedSyllabus;
}
