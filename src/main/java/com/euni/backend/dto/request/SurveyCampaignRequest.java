package com.euni.backend.dto.request;

import com.euni.backend.entity.enums.SurveyCampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyCampaignRequest {
    private String code;
    private String name;
    private String description;
    private UUID programId;
    private UUID workflowTemplateId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SurveyCampaignStatus status;
    private List<SurveyCampaignStepRequest> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurveyCampaignStepRequest {
        private Integer stepIndex;
        private String stepName;
        private LocalDateTime deadline;
        private List<String> requiredDocuments;
        private Map<String, Object> configuration;
    }
}
