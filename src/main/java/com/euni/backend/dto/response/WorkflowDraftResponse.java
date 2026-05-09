package com.euni.backend.dto.response;

import com.euni.backend.entity.enums.WorkflowStatus;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class WorkflowDraftResponse {
    private UUID id;
    private UUID templateId;
    private String code;
    private String name;
    private String description;
    private WorkflowStatus status;
    private String xmlContent;
    private ZonedDateTime lastSavedAt;
}
