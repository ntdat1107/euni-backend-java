package com.euni.backend.controller;

import com.euni.backend.dto.UserAiCredentialDto;
import com.euni.backend.dto.request.TestConnectionRequest;
import com.euni.backend.dto.request.UserAiCredentialRequest;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.dto.response.TestConnectionResponse;
import com.euni.backend.service.UserAiCredentialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user/ai-credentials")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserAiCredentialController extends BaseController {

    private final UserAiCredentialService credentialService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAiCredentialDto>>> getMyCredentials(Principal principal) {
        return ok(credentialService.getCredentialsByUsername(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAiCredentialDto>> createCredential(
            @Valid @RequestBody UserAiCredentialRequest request,
            Principal principal) {
        return ok("Lưu cấu hình API Key thành công", credentialService.createCredential(principal.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAiCredentialDto>> updateCredential(
            @PathVariable Long id,
            @Valid @RequestBody UserAiCredentialRequest request,
            Principal principal) {
        return ok("Cập nhật cấu hình API Key thành công", credentialService.updateCredential(principal.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCredential(@PathVariable Long id, Principal principal) {
        credentialService.deleteCredential(principal.getName(), id);
        return ok("Xóa cấu hình API Key thành công", null);
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<TestConnectionResponse>> testConnection(
            @RequestBody TestConnectionRequest request,
            Principal principal) {
        return ok(credentialService.testConnection(principal.getName(), request));
    }
}
