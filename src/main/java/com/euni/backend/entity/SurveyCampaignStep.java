package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "survey_campaign_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignStep extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private SurveyCampaign campaign;

    @Column(name = "step_index", nullable = false)
    private Integer stepIndex;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(columnDefinition = "TEXT")
    private String documents; // Stored as JSON string

    @Column(columnDefinition = "TEXT")
    private String configuration; // Dynamic configuration (JSON)
}
