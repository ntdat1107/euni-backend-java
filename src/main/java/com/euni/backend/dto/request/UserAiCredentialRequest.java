package com.euni.backend.dto.request;

import com.euni.backend.entity.enums.AiProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAiCredentialRequest {
    @NotNull(message = "Nhà cung cấp AI (Provider) không được để trống")
    private AiProvider provider;

    @NotBlank(message = "Tên gợi nhớ không được để trống")
    private String keyAlias;

    private String apiKey;

    private String baseUrl;

    @Builder.Default
    private Boolean active = true;
}
