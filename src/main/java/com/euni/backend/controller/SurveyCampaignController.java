package com.euni.backend.controller;

import com.euni.backend.dto.request.SurveyCampaignRequest;

import com.euni.backend.dto.SurveyCampaignDto;
import com.euni.backend.dto.SurveyCampaignStepDto;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.SurveyCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/survey/campaigns")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class SurveyCampaignController extends BaseController {

    private final SurveyCampaignService campaignService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SurveyCampaignDto>>> getAll() {
        return ok(campaignService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SurveyCampaignDto>> getById(@PathVariable UUID id) {
        return ok(campaignService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SurveyCampaignDto>> create(@RequestBody SurveyCampaignRequest request) {
        return ok("Tạo đợt khảo sát thành công", campaignService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SurveyCampaignDto>> update(@PathVariable UUID id, @RequestBody SurveyCampaignRequest request) {
        return ok("Cập nhật đợt khảo sát thành công", campaignService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        campaignService.delete(id);
        return ok("Đã hủy đợt khảo sát thành công", null);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SurveyCampaignDto>> cancel(@PathVariable UUID id) {
        // TODO: RBAC Permission Check (PERM_SURVEY_CAMPAIGN_CANCEL)
        return ok("Đã hủy đợt khảo sát thành công", campaignService.cancelCampaign(id));
    }

    @GetMapping("/check-code")
    public ResponseEntity<ApiResponse<Boolean>> checkCode(@RequestParam String code) {
        return ok(campaignService.checkCode(code));
    }

    @PostMapping("/{id}/steps/{stepId}/save")
    public ResponseEntity<ApiResponse<SurveyCampaignStepDto>> saveStepData(
            @PathVariable UUID id,
            @PathVariable UUID stepId,
            @RequestBody Map<String, Object> data) {
        String resultData = data.get("resultData") != null ? data.get("resultData").toString() : "{}";
        return ok(campaignService.saveStepData(id, stepId, resultData));
    }

    @GetMapping("/{id}/courses/{courseId}")
    public ResponseEntity<ApiResponse<com.euni.backend.dto.SurveyCampaignCourseDetailDto>> getCampaignCourseDetail(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        return ok(campaignService.getCampaignCourseDetail(id, courseId));
    }

    @PostMapping("/{id}/courses/{courseId}/save")
    public ResponseEntity<ApiResponse<com.euni.backend.dto.SurveyCampaignCourseDetailDto>> saveCampaignCourseDetail(
            @PathVariable UUID id,
            @PathVariable UUID courseId,
            @RequestBody Map<String, Object> data) {
        return ok("Lưu đề cương học phần thành công", campaignService.saveCampaignCourseDetail(id, courseId, data));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveCampaign(@PathVariable UUID id) {
        campaignService.approveCampaign(id);
        return ok("Phê duyệt đợt khảo sát và đồng bộ dữ liệu thành công", null);
    }
}

