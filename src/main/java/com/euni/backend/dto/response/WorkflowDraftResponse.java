package com.euni.backend.dto.response;

import com.euni.backend.entity.enums.WorkflowStatus;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class WorkflowDraftResponse {
    private Long id;
    private Long templateId;
    private String code;
    private String name;
    private String description;
    private WorkflowStatus status;
    private String jsonContent;
    private Integer version;
    private ZonedDateTime lastSavedAt;
}
