package com.euni.backend.controller;

import com.euni.backend.dto.SurveyFormDto;
import com.euni.backend.dto.SurveySubmissionDto;
import com.euni.backend.dto.SurveySummaryDto;
import com.euni.backend.dto.request.SurveyFormSaveRequest;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.SurveyFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/survey-forms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class SurveyFormController extends BaseController {

    private final SurveyFormService surveyFormService;

    @GetMapping("/step/{stepId}")
    public ResponseEntity<ApiResponse<SurveyFormDto>> getOrCreateFormForStep(
            @PathVariable Long stepId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Lấy thông tin biểu mẫu thành công", surveyFormService.getOrCreateFormForStep(stepId, username));
    }

    @PostMapping("/step/{stepId}")
    public ResponseEntity<ApiResponse<SurveyFormDto>> saveForm(
            @PathVariable Long stepId,
            @Valid @RequestBody SurveyFormSaveRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Lưu thiết kế biểu mẫu khảo sát thành công", surveyFormService.saveForm(stepId, request, username));
    }

    @PostMapping("/step/{stepId}/publish")
    public ResponseEntity<ApiResponse<SurveyFormDto>> publishForm(
            @PathVariable Long stepId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Xuất bản khảo sát thành công", surveyFormService.publishForm(stepId, username));
    }

    @PostMapping("/step/{stepId}/close")
    public ResponseEntity<ApiResponse<SurveyFormDto>> closeForm(
            @PathVariable Long stepId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Đóng đợt khảo sát thành công", surveyFormService.closeForm(stepId, username));
    }

    @GetMapping("/step/{stepId}/submissions")
    public ResponseEntity<ApiResponse<List<SurveySubmissionDto>>> getFormSubmissions(
            @PathVariable Long stepId
    ) {
        return ok("Lấy danh sách phản hồi thành công", surveyFormService.getFormSubmissions(stepId));
    }

    @GetMapping("/step/{stepId}/summary")
    public ResponseEntity<ApiResponse<SurveySummaryDto>> getFormSummary(
            @PathVariable Long stepId
    ) {
        return ok("Lấy tổng hợp số liệu khảo sát thành công", surveyFormService.getFormSummary(stepId));
    }
}
