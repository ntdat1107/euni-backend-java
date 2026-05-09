package com.euni.backend.dto.response;

import com.euni.backend.entity.enums.WorkflowStatus;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class WorkflowTemplateResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private WorkflowStatus status;
    private String xmlContent;
    private Integer version;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean hasDraft;
}
