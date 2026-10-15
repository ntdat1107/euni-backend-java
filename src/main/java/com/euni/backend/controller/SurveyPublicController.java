package com.euni.backend.controller;

import com.euni.backend.dto.SurveyFormDto;
import com.euni.backend.dto.request.SurveySubmissionRequest;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.SurveyFormService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/surveys")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class SurveyPublicController extends BaseController {

    private final SurveyFormService surveyFormService;

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<SurveyFormDto>> getPublicForm(@PathVariable String slug) {
        return ok("Lấy thông tin biểu mẫu thành công", surveyFormService.getPublicForm(slug));
    }

    @PostMapping("/{slug}/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitSurvey(
            @PathVariable String slug,
            @Valid @RequestBody SurveySubmissionRequest request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = servletRequest.getRemoteAddr();
        }
        return ok("Gửi câu trả lời khảo sát thành công", surveyFormService.submitPublicSurvey(slug, request, ipAddress));
    }
}
