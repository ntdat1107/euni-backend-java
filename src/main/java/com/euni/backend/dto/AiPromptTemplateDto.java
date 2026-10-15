package com.euni.backend.dto;

import com.euni.backend.entity.AiPromptTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPromptTemplateDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String templateContent;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public static AiPromptTemplateDto fromEntity(AiPromptTemplate entity) {
        if (entity == null) return null;
        return AiPromptTemplateDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .templateContent(entity.getTemplateContent())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
