package com.euni.backend.dto;

import com.euni.backend.entity.enums.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAiModelConfigDto {
    private Long id;
    private Long credentialId;
    private String credentialAlias;
    private AiProvider provider;
    private String taskType;
    private String modelName;
    private Double temperature;
    private Integer maxTokens;
    private boolean active;
}
