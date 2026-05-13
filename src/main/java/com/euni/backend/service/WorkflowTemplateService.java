package com.euni.backend.service;

import com.euni.backend.dto.request.WorkflowTemplateRequest;
import com.euni.backend.dto.response.WorkflowDraftResponse;
import com.euni.backend.dto.response.WorkflowTemplateResponse;
import com.euni.backend.entity.WorkflowTemplate;
import com.euni.backend.entity.WorkflowTemplateDraft;
import com.euni.backend.repository.WorkflowTemplateDraftRepository;
import com.euni.backend.repository.WorkflowTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.euni.backend.entity.enums.WorkflowStatus;
import com.euni.backend.repository.WorkflowTemplateDraftRepository;

@Service
@RequiredArgsConstructor
public class WorkflowTemplateService {
    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowTemplateDraftRepository draftRepository;

    public List<WorkflowTemplateResponse> getAllLatestTemplates() {
        return templateRepository.findLatestTemplates().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public WorkflowTemplateResponse getTemplateById(UUID id) {
        WorkflowTemplate template = templateRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        WorkflowTemplateResponse response = mapToResponse(template);
        response.setHasDraft(draftRepository.findByTemplateId(id).isPresent());
        return response;
    }

    public List<WorkflowTemplateResponse> getHistoryByTemplateId(UUID id) {
        WorkflowTemplate template = templateRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        return templateRepository.findAllVersionsByCode(template.getCode()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkflowTemplateResponse saveOfficialTemplate(WorkflowTemplateRequest request) {
        // 1. Calculate next version (including deleted versions to maintain history)
        int nextVersion = templateRepository.getMaxVersionIncludingDeleted(request.getCode()) + 1;

        // 2. Mark all old versions as inactive (but NOT deleted, so they can still be referenced by history)
        List<WorkflowTemplate> oldVersions = templateRepository.findByCodeOrderByVersionDesc(request.getCode());
        oldVersions.forEach(t -> {
            t.setIsActive(false);
            templateRepository.save(t);
        });

        // 3. Create new version
        WorkflowTemplate newTemplate = WorkflowTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : WorkflowStatus.ACTIVE)
                .jsonContent(request.getJsonContent())
                .version(nextVersion)
                .isActive(true)
                .build();
        
        WorkflowTemplate saved = templateRepository.save(newTemplate);
        
        // Clear draft for this code/template after official save
        draftRepository.findByCode(request.getCode()).ifPresent(draftRepository::delete);
        
        return mapToResponse(saved);
    }

    @Transactional
    public WorkflowDraftResponse syncDraft(WorkflowTemplateRequest request) {
        WorkflowTemplateDraft draft = draftRepository.findByCode(request.getCode())
                .orElse(WorkflowTemplateDraft.builder().code(request.getCode()).build());
        
        draft.setName(request.getName());
        draft.setDescription(request.getDescription());
        draft.setStatus(request.getStatus());
        draft.setJsonContent(request.getJsonContent());
        
        // Link to template if code exists
        templateRepository.findFirstByCodeOrderByVersionDesc(request.getCode())
                .ifPresent(t -> draft.setTemplateId(t.getId()));
        
        WorkflowTemplateDraft saved = draftRepository.save(draft);
        
        return WorkflowDraftResponse.builder()
                .id(saved.getId())
                .templateId(saved.getTemplateId())
                .code(saved.getCode())
                .name(saved.getName())
                .description(saved.getDescription())
                .status(saved.getStatus())
                .jsonContent(saved.getJsonContent())
                .lastSavedAt(saved.getLastSavedAt())
                .build();
    }

    public Optional<WorkflowDraftResponse> getDraftByCode(String code) {
        return draftRepository.findByCode(code)
                .map(d -> WorkflowDraftResponse.builder()
                        .id(d.getId())
                        .templateId(d.getTemplateId())
                        .code(d.getCode())
                        .name(d.getName())
                        .description(d.getDescription())
                        .status(d.getStatus())
                        .jsonContent(d.getJsonContent())
                        .lastSavedAt(d.getLastSavedAt())
                        .build());
    }

    public boolean checkCodeExists(String code, UUID currentId) {
        return templateRepository.findFirstByCodeOrderByVersionDesc(code)
                .map(t -> !t.getId().equals(currentId))
                .orElse(false);
    }

    @Transactional
    public WorkflowTemplateResponse updateStatus(UUID id, WorkflowStatus status) {
        WorkflowTemplate template = templateRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        template.setStatus(status);
        template.setIsActive(status == WorkflowStatus.ACTIVE);
        
        WorkflowTemplate saved = templateRepository.save(template);
        return mapToResponse(saved);
    }

    private WorkflowTemplateResponse mapToResponse(WorkflowTemplate template) {
        return WorkflowTemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .description(template.getDescription())
                .status(template.getStatus())
                .jsonContent(template.getJsonContent())
                .version(template.getVersion())
                .isActive(template.getIsActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
