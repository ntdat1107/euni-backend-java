package com.euni.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignDto {
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
    private String status;
    private List<SurveyCampaignStepDto> steps;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
