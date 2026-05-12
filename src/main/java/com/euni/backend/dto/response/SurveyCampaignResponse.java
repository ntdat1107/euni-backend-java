package com.euni.backend.dto.response;

import com.euni.backend.entity.enums.SurveyCampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private UUID programId;
    private String programName;
    private UUID workflowTemplateId;
    private String workflowTemplateName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SurveyCampaignStatus status;
    private List<SurveyCampaignStepResponse> steps;
    private ZonedDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SurveyCampaignStepResponse {
        private UUID id;
        private Integer stepIndex;
        private String stepName;
        private LocalDateTime deadline;
        private List<String> requiredDocuments;
        private Map<String, Object> configuration;
    }
}
