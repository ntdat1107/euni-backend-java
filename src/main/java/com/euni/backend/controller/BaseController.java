package com.euni.backend.controller;

import com.euni.backend.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    
    protected <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    protected ResponseEntity<ApiResponse<Void>> error(String message) {
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }
}
