package com.euni.backend.dto.request;

import com.euni.backend.entity.enums.WorkflowStatus;
import lombok.Data;

@Data
public class WorkflowTemplateRequest {
    private String code;
    private String name;
    private String description;
    private WorkflowStatus status;
    private String jsonContent;
    private Long draftId;
}
