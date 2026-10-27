package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "courses_id_seq")
    @SequenceGenerator(name = "courses_id_seq", sequenceName = "courses_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Builder.Default
    private Integer credits = 3;

    @Column(name = "theory_credits")
    @Builder.Default
    private Integer theoryCredits = 2;

    @Column(name = "practice_credits")
    @Builder.Default
    private Integer practiceCredits = 1;

    @Column(name = "self_study_hours")
    @Builder.Default
    private Integer selfStudyHours = 90;

    private String description;
}
