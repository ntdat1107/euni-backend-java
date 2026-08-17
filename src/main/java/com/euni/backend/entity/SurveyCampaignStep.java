package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_campaign_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignStep extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_campaign_steps_id_seq")
    @SequenceGenerator(name = "survey_campaign_steps_id_seq", sequenceName = "survey_campaign_steps_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private SurveyCampaign campaign;

    @Column(name = "step_index", nullable = false)
    private Integer stepIndex;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "required_documents", columnDefinition = "TEXT")
    private String requiredDocuments; // JSON string array

    @Column(columnDefinition = "TEXT")
    private String configuration; // Dynamic configuration (JSON)

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData; // Actual survey output (JSON)
}
