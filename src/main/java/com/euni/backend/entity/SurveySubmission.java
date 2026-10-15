package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveySubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_submissions_id_seq")
    @SequenceGenerator(name = "survey_submissions_id_seq", sequenceName = "survey_submissions_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private SurveyForm form;

    @Column(name = "respondent_name")
    private String respondentName;

    @Column(name = "respondent_email")
    private String respondentEmail;

    @Column(name = "respondent_phone", length = 50)
    private String respondentPhone;

    @Column(name = "organization")
    private String organization;

    @Column(name = "position")
    private String position;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private ZonedDateTime submittedAt = ZonedDateTime.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurveyAnswer> answers = new ArrayList<>();
}
