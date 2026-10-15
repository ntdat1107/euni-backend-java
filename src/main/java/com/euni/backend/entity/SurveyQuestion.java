package com.euni.backend.entity;

import com.euni.backend.entity.enums.SurveyQuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "survey_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_questions_id_seq")
    @SequenceGenerator(name = "survey_questions_id_seq", sequenceName = "survey_questions_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private SurveyForm form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private SurveySection section;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "help_text", columnDefinition = "TEXT")
    private String helpText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", length = 30, nullable = false)
    private SurveyQuestionType questionType;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private boolean required = true;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "min_rating")
    @Builder.Default
    private Integer minRating = 1;

    @Column(name = "max_rating")
    @Builder.Default
    private Integer maxRating = 5;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private Set<SurveyQuestionOption> options = new LinkedHashSet<>();
}
