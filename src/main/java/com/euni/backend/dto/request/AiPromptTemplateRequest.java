package com.euni.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPromptTemplateRequest {

    private String code;
    private String name;
    private String description;
    private String templateContent;
    private Boolean active;
}
