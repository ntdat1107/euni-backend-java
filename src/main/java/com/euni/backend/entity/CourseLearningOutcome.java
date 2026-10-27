package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_learning_outcomes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"syllabus_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLearningOutcome extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_learning_outcomes_id_seq")
    @SequenceGenerator(name = "course_learning_outcomes_id_seq", sequenceName = "course_learning_outcomes_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "bloom_level", length = 20)
    @Builder.Default
    private String bloomLevel = "APPLY";

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 1;
}
