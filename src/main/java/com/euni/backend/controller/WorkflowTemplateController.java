package com.euni.backend.controller;

import com.euni.backend.dto.request.WorkflowTemplateRequest;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.dto.response.WorkflowDraftResponse;
import com.euni.backend.dto.response.WorkflowTemplateResponse;
import com.euni.backend.entity.enums.WorkflowStatus;
import com.euni.backend.service.WorkflowTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow-templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class WorkflowTemplateController extends BaseController {
    private final WorkflowTemplateService templateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkflowTemplateResponse>>> getAll() {
        return ok(templateService.getAllLatestTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowTemplateResponse>> getById(@PathVariable UUID id) {
        return ok(templateService.getTemplateById(id));
    }

    @PostMapping("/save-official")
    public ResponseEntity<ApiResponse<WorkflowTemplateResponse>> saveOfficial(@RequestBody WorkflowTemplateRequest request) {
        return ok(templateService.saveOfficialTemplate(request));
    }

    @PostMapping("/sync-draft")
    public ResponseEntity<ApiResponse<WorkflowDraftResponse>> syncDraft(@RequestBody WorkflowTemplateRequest request) {
        return ok(templateService.syncDraft(request));
    }

    @GetMapping("/draft/{code}")
    public ResponseEntity<ApiResponse<WorkflowDraftResponse>> getDraft(@PathVariable String code) {
        return templateService.getDraftByCode(code)
                .map(this::ok)
                .orElse(ok(null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<WorkflowTemplateResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        WorkflowStatus status = WorkflowStatus.valueOf(body.get("status").toUpperCase());
        return ok(templateService.updateStatus(id, status));
    }
}
