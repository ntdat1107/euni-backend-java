package com.euni.backend.dto.request;

import com.euni.backend.entity.enums.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestConnectionRequest {
    private AiProvider provider;
    private String apiKey;
    private Long credentialId;
    private String baseUrl;
    private String modelName;
}
