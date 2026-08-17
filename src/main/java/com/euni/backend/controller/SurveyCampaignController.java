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
    public ResponseEntity<ApiResponse<SurveyCampaignDto>> getById(@PathVariable Long id) {
        return ok(campaignService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SurveyCampaignDto>> create(@RequestBody SurveyCampaignRequest request) {
        return ok("Tạo đợt khảo sát thành công", campaignService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SurveyCampaignDto>> update(@PathVariable Long id, @RequestBody SurveyCampaignRequest request) {
        return ok("Cập nhật đợt khảo sát thành công", campaignService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return ok("Đã hủy đợt khảo sát thành công", null);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SurveyCampaignDto>> cancel(@PathVariable Long id) {
        // TODO: RBAC Permission Check (PERM_SURVEY_CAMPAIGN_CANCEL)
        return ok("Đã hủy đợt khảo sát thành công", campaignService.cancelCampaign(id));
    }

    @GetMapping("/check-code")
    public ResponseEntity<ApiResponse<Boolean>> checkCode(@RequestParam String code) {
        return ok(campaignService.checkCode(code));
    }

    @PostMapping("/{id}/steps/{stepId}/save")
    public ResponseEntity<ApiResponse<SurveyCampaignStepDto>> saveStepData(
            @PathVariable Long id,
            @PathVariable Long stepId,
            @RequestBody Map<String, Object> data) {
        String resultData = data.get("resultData") != null ? data.get("resultData").toString() : "{}";
        return ok(campaignService.saveStepData(id, stepId, resultData));
    }

    @GetMapping("/{id}/courses/{courseId}")
    public ResponseEntity<ApiResponse<com.euni.backend.dto.SurveyCampaignCourseDetailDto>> getCampaignCourseDetail(
            @PathVariable Long id,
            @PathVariable Long courseId) {
        return ok(campaignService.getCampaignCourseDetail(id, courseId));
    }

    @PostMapping("/{id}/courses/{courseId}/save")
    public ResponseEntity<ApiResponse<com.euni.backend.dto.SurveyCampaignCourseDetailDto>> saveCampaignCourseDetail(
            @PathVariable Long id,
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> data) {
        return ok("Lưu đề cương học phần thành công", campaignService.saveCampaignCourseDetail(id, courseId, data));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveCampaign(@PathVariable Long id) {
        campaignService.approveCampaign(id);
        return ok("Phê duyệt đợt khảo sát và đồng bộ dữ liệu thành công", null);
    }
}
