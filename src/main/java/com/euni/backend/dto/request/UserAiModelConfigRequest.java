package com.euni.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAiModelConfigRequest {
    private Long credentialId;

    @NotBlank(message = "Loại tác vụ (task_type) không được để trống")
    private String taskType;

    @NotBlank(message = "Tên mô hình (model_name) không được để trống")
    private String modelName;

    @Builder.Default
    private Double temperature = 0.5;

    @Builder.Default
    private Integer maxTokens = 4096;

    @Builder.Default
    private Boolean active = true;
}
