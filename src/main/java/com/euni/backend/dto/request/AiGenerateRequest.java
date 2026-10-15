package com.euni.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateRequest {
    private String prompt;
    private String action;
    private String contentType;
    private Map<String, Object> metadata;
}


