package com.euni.backend.dto;

import com.euni.backend.entity.enums.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAiCredentialDto {
    private Long id;
    private AiProvider provider;
    private String keyAlias;
    private String maskedKey;
    private String baseUrl;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
