package com.euni.backend.controller;

import com.euni.backend.dto.request.LoginRequest;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.dto.response.TokenResponse;
import com.euni.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController extends BaseController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody java.util.Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token is required");
        }
        return ok(authService.refreshToken(refreshToken));
    }
}
