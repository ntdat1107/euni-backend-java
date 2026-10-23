package com.euni.backend.dto;

import com.euni.backend.entity.enums.SurveyFormStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyFormVersionDto {
    private Long id;
    private Long campaignStepId;
    private Integer version;
    private SurveyFormStatus status;
    private String title;
    private String publicSlug;
    private long totalSubmissions;
    private Boolean isCurrent;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
