package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "syllabus_assignments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"program_course_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "syllabus_assignments_id_seq")
    @SequenceGenerator(name = "syllabus_assignments_id_seq", sequenceName = "syllabus_assignments_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_course_id", nullable = false)
    private ProgramCourse programCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50, nullable = false)
    @Builder.Default
    private String role = "PRIMARY_AUTHOR"; // PRIMARY_AUTHOR, CONTRIBUTOR

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private ZonedDateTime assignedAt = ZonedDateTime.now();
}
