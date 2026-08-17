package com.euni.backend.dto;

import com.euni.backend.dto.response.WorkflowTemplateResponse;
import lombok.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long programId;
    private String programName;
    private ProgramDto program;
    private Long workflowTemplateId;
    private String workflowTemplateName;
    private WorkflowTemplateResponse workflowTemplate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private List<SurveyCampaignCourseMetaDto> courses;
    private List<SurveyCampaignStepDto> steps;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
