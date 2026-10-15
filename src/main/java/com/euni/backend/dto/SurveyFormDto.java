package com.euni.backend.dto;

import com.euni.backend.entity.enums.SurveyFormStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyFormDto {
    private Long id;
    private Long campaignStepId;
    private Long programId;
    private String programCode;
    private String programName;
    private String title;
    private String description;
    private String targetAudience;
    private String publicSlug;
    private String publicUrl;
    private SurveyFormStatus status;
    private boolean anonymous;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private long totalSubmissions;
    @Builder.Default
    private List<SurveySectionDto> sections = new ArrayList<>();
    @Builder.Default
    private List<SurveyQuestionDto> directQuestions = new ArrayList<>();
}
