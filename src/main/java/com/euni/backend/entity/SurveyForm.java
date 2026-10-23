package com.euni.backend.entity;

import com.euni.backend.entity.enums.SurveyFormStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "survey_forms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SurveyForm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_forms_id_seq")
    @SequenceGenerator(name = "survey_forms_id_seq", sequenceName = "survey_forms_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_step_id")
    private SurveyCampaignStep campaignStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_audience")
    private String targetAudience;

    @Column(name = "public_slug", unique = true, length = 100)
    private String publicSlug;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = true;

    @Column(name = "previous_version_id")
    private Long previousVersionId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private SurveyFormStatus status = SurveyFormStatus.DRAFT;

    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private boolean anonymous = false;

    @Column(name = "start_date")
    private ZonedDateTime startDate;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private Set<SurveySection> sections = new LinkedHashSet<>();

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private Set<SurveyQuestion> questions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SurveySubmission> submissions = new LinkedHashSet<>();

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}
