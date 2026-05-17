package com.euni.backend.controller;

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

    @PostMapping("/{id}/steps/{stepId}/save")
    public ResponseEntity<ApiResponse<SurveyCampaignStepDto>> saveStepData(
            @PathVariable UUID id,
            @PathVariable UUID stepId,
            @RequestBody Map<String, Object> data) {
        String resultData = data.get("resultData") != null ? data.get("resultData").toString() : "{}";
        return ok(campaignService.saveStepData(id, stepId, resultData));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveCampaign(@PathVariable UUID id) {
        campaignService.approveCampaign(id);
        return ok("Phê duyệt đợt khảo sát và đồng bộ dữ liệu thành công", null);
    }
}
