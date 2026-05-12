package com.euni.backend.controller;

import com.euni.backend.dto.request.SurveyCampaignRequest;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.dto.response.SurveyCampaignResponse;
import com.euni.backend.service.SurveyCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/survey/campaigns")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class SurveyCampaignController extends BaseController {
    private final SurveyCampaignService campaignService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SurveyCampaignResponse>>> getAll() {
        return ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SurveyCampaignResponse>> getById(@PathVariable UUID id) {
        return ok(campaignService.getCampaignById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SurveyCampaignResponse>> create(@RequestBody SurveyCampaignRequest request) {
        return ok(campaignService.createCampaign(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SurveyCampaignResponse>> update(@PathVariable UUID id, @RequestBody SurveyCampaignRequest request) {
        return ok(campaignService.updateCampaign(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        campaignService.deleteCampaign(id);
        return ok(null);
    }
}
