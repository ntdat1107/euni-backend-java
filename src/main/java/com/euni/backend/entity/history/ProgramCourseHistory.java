package com.euni.backend.entity.history;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "program_course_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramCourseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_course_history_id_seq")
    @SequenceGenerator(name = "program_course_history_id_seq", sequenceName = "program_course_history_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "program_course_id", nullable = false)
    private Long programCourseId;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    private Integer semester;

    @Column(name = "is_required")
    private Boolean required;

    @Column(length = 20)
    private String action; // ADDED, REMOVED, RESTORED, UPDATED

    @CreationTimestamp
    @Column(name = "changed_at")
    private ZonedDateTime changedAt;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "change_reason")
    private String changeReason;
}
