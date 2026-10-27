package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_dependencies", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"program_id", "course_id", "prerequisite_course_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDependency extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_dependencies_id_seq")
    @SequenceGenerator(name = "course_dependencies_id_seq", sequenceName = "course_dependencies_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    private Course prerequisiteCourse;

    @Column(name = "dependency_type", length = 20, nullable = false)
    @Builder.Default
    private String dependencyType = "PREREQUISITE"; // PREREQUISITE, PRE_COURSE, COREQUISITE
}
