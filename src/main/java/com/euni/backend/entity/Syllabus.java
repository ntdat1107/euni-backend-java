package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;

@Entity
@Table(name = "syllabuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Syllabus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "syllabuses_id_seq")
    @SequenceGenerator(name = "syllabuses_id_seq", sequenceName = "syllabuses_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_course_id", nullable = false)
    private ProgramCourse programCourse;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String version = "1.0";

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, REVIEWING, APPROVED, DEPRECATED

    @Column(name = "course_description", columnDefinition = "TEXT")
    private String courseDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "course_objectives", columnDefinition = "json")
    private String courseObjectives;

    @Column(name = "teaching_methods", columnDefinition = "TEXT")
    private String teachingMethods;

    @Column(name = "student_duties", columnDefinition = "TEXT")
    private String studentDuties;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assessment_scheme", columnDefinition = "json")
    private String assessmentScheme;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weekly_schedule", columnDefinition = "json")
    private String weeklySchedule;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "textbooks", columnDefinition = "json")
    private String textbooks;

    @Column(name = "facilities_requirements", columnDefinition = "TEXT")
    private String facilitiesRequirements;

    @Column(name = "is_master", nullable = false)
    @Builder.Default
    private Boolean isMaster = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private ZonedDateTime approvedAt;
}
