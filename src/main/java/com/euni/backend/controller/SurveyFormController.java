package com.euni.backend.controller;

import com.euni.backend.dto.SurveyFormDto;
import com.euni.backend.dto.SurveyFormVersionDto;
import com.euni.backend.dto.SurveySubmissionDto;
import com.euni.backend.dto.SurveySummaryDto;
import com.euni.backend.dto.request.SurveyFormSaveRequest;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.SurveyFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        return ok("Lấy thông tin biểu mẫu thành công", surveyFormService.getFormForStep(stepId));
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

    @PostMapping("/step/{stepId}/submit-review")
    public ResponseEntity<ApiResponse<SurveyFormDto>> submitReview(
            @PathVariable Long stepId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Đã nộp biểu mẫu để cấp có thẩm quyền phê duyệt", surveyFormService.submitReview(stepId, username));
    }

    @PostMapping("/step/{stepId}/approve-design")
    public ResponseEntity<ApiResponse<SurveyFormDto>> approveDesign(
            @PathVariable Long stepId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Đã phê duyệt thiết kế biểu mẫu thành công", surveyFormService.approveDesign(stepId, username));
    }

    @PostMapping("/step/{stepId}/recall")
    public ResponseEntity<ApiResponse<SurveyFormDto>> recallSubmission(
            @PathVariable Long stepId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Đã thu hồi yêu cầu phê duyệt biểu mẫu", surveyFormService.recallSubmission(stepId, username));
    }

    @PostMapping("/step/{stepId}/reject-design")
    public ResponseEntity<ApiResponse<SurveyFormDto>> rejectDesign(
            @PathVariable Long stepId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        String reason = body != null ? body.get("reason") : "";
        return ok("Đã gửi yêu cầu chỉnh sửa thiết kế biểu mẫu", surveyFormService.rejectDesign(stepId, reason, username));
    }

    @PostMapping("/step/{stepId}/publish")
    public ResponseEntity<ApiResponse<SurveyFormDto>> publishForm(
            @PathVariable Long stepId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Xuất bản khảo sát thành công", surveyFormService.publishForm(stepId, username));
    }

    @PostMapping("/step/{stepId}/request-revision")
    public ResponseEntity<ApiResponse<SurveyFormDto>> requestRevision(
            @PathVariable Long stepId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Đã kích hoạt tạo phiên bản mới và cho phép chỉnh sửa biểu mẫu", surveyFormService.requestRevision(stepId, username));
    }

    @PostMapping("/step/{stepId}/close")
    public ResponseEntity<ApiResponse<SurveyFormDto>> closeForm(
            @PathVariable Long stepId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "system";
        return ok("Đóng đợt khảo sát thành công", surveyFormService.closeForm(stepId, username));
    }

    @GetMapping("/step/{stepId}/versions")
    public ResponseEntity<ApiResponse<List<SurveyFormVersionDto>>> getFormVersions(
            @PathVariable Long stepId
    ) {
        return ok("Lấy danh sách các phiên bản biểu mẫu thành công", surveyFormService.getFormVersions(stepId));
    }

    @GetMapping("/version/{formId}")
    public ResponseEntity<ApiResponse<SurveyFormDto>> getFormById(
            @PathVariable Long formId
    ) {
        return ok("Lấy chi tiết phiên bản biểu mẫu thành công", surveyFormService.getFormById(formId));
    }

    @GetMapping("/step/{stepId}/submissions")
    public ResponseEntity<ApiResponse<List<SurveySubmissionDto>>> getFormSubmissions(
            @PathVariable Long stepId
    ) {
        return ok("Lấy danh sách phản hồi thành công", surveyFormService.getFormSubmissions(stepId));
    }

    @GetMapping("/version/{formId}/submissions")
    public ResponseEntity<ApiResponse<List<SurveySubmissionDto>>> getFormSubmissionsByFormId(
            @PathVariable Long formId
    ) {
        return ok("Lấy danh sách phản hồi theo phiên bản thành công", surveyFormService.getFormSubmissionsByFormId(formId));
    }

    @GetMapping("/step/{stepId}/summary")
    public ResponseEntity<ApiResponse<SurveySummaryDto>> getFormSummary(
            @PathVariable Long stepId
    ) {
        return ok("Lấy tổng hợp số liệu khảo sát thành công", surveyFormService.getFormSummary(stepId));
    }

    @GetMapping("/version/{formId}/summary")
    public ResponseEntity<ApiResponse<SurveySummaryDto>> getFormSummaryByFormId(
            @PathVariable Long formId
    ) {
        return ok("Lấy tổng hợp số liệu khảo sát theo phiên bản thành công", surveyFormService.getFormSummaryByFormId(formId));
    }
}
