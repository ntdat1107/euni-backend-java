package com.euni.backend.entity.history;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "program_course_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramCourseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "program_course_id", nullable = false)
    private UUID programCourseId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    private Integer semester;

    @Column(name = "is_required")
    private Boolean required;

    @Column(length = 20)
    private String action; // ADDED, REMOVED, RESTORED, UPDATED

    @CreationTimestamp
    @Column(name = "changed_at")
    private ZonedDateTime changedAt;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "change_reason")
    private String changeReason;
}
