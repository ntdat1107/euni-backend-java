package com.euni.backend.controller;

import com.euni.backend.dto.AiPromptTemplateDto;
import com.euni.backend.dto.request.AiPromptTemplateRequest;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.AiPromptTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/prompt-templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AiPromptTemplateController extends BaseController {

    private final AiPromptTemplateService promptTemplateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiPromptTemplateDto>>> getAll() {
        return ok(promptTemplateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AiPromptTemplateDto>> getById(@PathVariable Long id) {
        return ok(promptTemplateService.getTemplateById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<AiPromptTemplateDto>> getByCode(@PathVariable String code) {
        return ok(promptTemplateService.getTemplateByCode(code));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiPromptTemplateDto>> create(@RequestBody AiPromptTemplateRequest request) {
        return ok(promptTemplateService.createTemplate(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiPromptTemplateDto>> update(
            @PathVariable Long id,
            @RequestBody AiPromptTemplateRequest request) {
        return ok(promptTemplateService.updateTemplate(id, request));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<AiPromptTemplateDto>> toggleActive(@PathVariable Long id) {
        return ok(promptTemplateService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        promptTemplateService.deleteTemplate(id);
        return ok(null);
    }
}
