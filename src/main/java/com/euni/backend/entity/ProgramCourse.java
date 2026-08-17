package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GeneratedColumn;

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

    private Integer semester;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean required = true;

    @Column(name = "data", columnDefinition = "TEXT")
    private String data; // Synced metadata (CLO JSON)
}
