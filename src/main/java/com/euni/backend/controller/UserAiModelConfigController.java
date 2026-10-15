package com.euni.backend.controller;

import com.euni.backend.dto.UserAiModelConfigDto;
import com.euni.backend.dto.request.UserAiModelConfigRequest;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.UserAiModelConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user/ai-model-configs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserAiModelConfigController extends BaseController {

    private final UserAiModelConfigService modelConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAiModelConfigDto>>> getMyModelConfigs(Principal principal) {
        return ok(modelConfigService.getModelConfigs(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAiModelConfigDto>> saveOrUpdateModelConfig(
            @Valid @RequestBody UserAiModelConfigRequest request,
            Principal principal) {
        return ok("Lưu cấu hình mô hình thành công", modelConfigService.saveOrUpdateModelConfig(principal.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteModelConfig(@PathVariable Long id, Principal principal) {
        modelConfigService.deleteModelConfig(principal.getName(), id);
        return ok("Xóa cấu hình mô hình thành công", null);
    }
}
