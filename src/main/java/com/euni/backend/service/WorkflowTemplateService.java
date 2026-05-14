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
        List<WorkflowTemplate> officialTemplates = templateRepository.findLatestTemplates();
        List<WorkflowTemplateDraft> drafts = draftRepository.findAllActive();

        List<WorkflowTemplateResponse> responses = officialTemplates.stream()
                .map(this::mapToResponse)
                .peek(r -> r.setHasDraft(drafts.stream().anyMatch(d -> d.getCode().equals(r.getCode()))))
                .collect(Collectors.toList());

        // Add standalone drafts
        drafts.stream()
                .filter(d -> officialTemplates.stream().noneMatch(o -> o.getCode().equals(d.getCode())))
                .map(this::mapDraftToResponse)
                .forEach(responses::add);

        return responses;
    }

    public WorkflowTemplateResponse getTemplateById(UUID id) {
        Optional<WorkflowTemplate> templateOpt = templateRepository.findByIdIncludingDeleted(id);
        if (templateOpt.isPresent()) {
            WorkflowTemplate template = templateOpt.get();
            WorkflowTemplateResponse response = mapToResponse(template);
            // Check by code to be sure we detect it even if viewing older versions
            response.setHasDraft(draftRepository.findByCode(template.getCode()).isPresent());
            return response;
        }

        // Fallback to active draft
        return draftRepository.findActiveById(id)
                .map(this::mapDraftToResponse)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    public List<WorkflowTemplateResponse> getHistoryByTemplateId(UUID id) {
        String code = null;
        
        // Try finding in official templates
        Optional<WorkflowTemplate> templateOpt = templateRepository.findByIdIncludingDeleted(id);
        if (templateOpt.isPresent()) {
            code = templateOpt.get().getCode();
        } else {
            // Try finding in active drafts
            Optional<WorkflowTemplateDraft> draftOpt = draftRepository.findActiveById(id);
            if (draftOpt.isPresent()) {
                code = draftOpt.get().getCode();
            }
        }

        if (code == null) {
            throw new RuntimeException("Template not found");
        }

        return templateRepository.findAllVersionsByCode(code).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkflowTemplateResponse saveOfficialTemplate(WorkflowTemplateRequest request) {
        // 0. Validate draft if draftId is provided
        if (request.getDraftId() != null) {
            WorkflowTemplateDraft draft = draftRepository.findById(request.getDraftId())
                    .orElseThrow(() -> new RuntimeException("Bản nháp không tồn tại."));
            
            if (draft.isDeleted()) {
                throw new RuntimeException("Bản nháp này đã được xuất bản trước đó và không còn giá trị.");
            }
        }

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
        
        // Soft delete draft for this code/template after official save
        draftRepository.findByCode(request.getCode()).ifPresent(draft -> {
            draft.setDeleted(true);
            draftRepository.save(draft);
        });
        
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
        templateRepository.findFirstByCodeAndDeletedFalseOrderByVersionDesc(request.getCode())
                .ifPresent(t -> draft.setTemplateId(t.getId()));
        
        WorkflowTemplateDraft saved = draftRepository.save(draft);
        
        return mapToDraftResponse(saved);
    }

    public Optional<WorkflowDraftResponse> getDraftByCode(String code) {
        return draftRepository.findByCode(code)
                .map(this::mapToDraftResponse);
    }

    public boolean checkCodeExists(String code, UUID currentId) {
        return templateRepository.findFirstByCodeAndDeletedFalseOrderByVersionDesc(code)
                .map(t -> !t.getId().equals(currentId))
                .orElse(false);
    }

    public Optional<WorkflowTemplateResponse> getLatestTemplateByCode(String code) {
        return templateRepository.findFirstByCodeAndDeletedFalseOrderByVersionDesc(code)
                .map(this::mapToResponse);
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

    private WorkflowTemplateResponse mapDraftToResponse(WorkflowTemplateDraft draft) {
        int baseVersion = 0;
        if (draft.getCode() != null) {
            baseVersion = templateRepository.getMaxVersionIncludingDeleted(draft.getCode());
        }

        return WorkflowTemplateResponse.builder()
                .id(draft.getId())
                .code(draft.getCode())
                .name(draft.getName())
                .description(draft.getDescription())
                .status(WorkflowStatus.DRAFT)
                .jsonContent(draft.getJsonContent())
                .version(baseVersion + 1) // Show next version number for draft
                .isActive(true) // Mark as "Active" for the editor so it doesn't trigger history view
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .hasDraft(true)
                .build();
    }

    private WorkflowDraftResponse mapToDraftResponse(WorkflowTemplateDraft draft) {
        Integer baseVersion = 0;
        if (draft.getCode() != null) {
            baseVersion = templateRepository.findFirstByCodeAndDeletedFalseOrderByVersionDesc(draft.getCode())
                    .map(WorkflowTemplate::getVersion)
                    .orElse(0);
        }

        return WorkflowDraftResponse.builder()
                .id(draft.getId())
                .templateId(draft.getTemplateId())
                .code(draft.getCode())
                .name(draft.getName())
                .description(draft.getDescription())
                .status(draft.getStatus())
                .jsonContent(draft.getJsonContent())
                .version(baseVersion + 1)
                .lastSavedAt(draft.getLastSavedAt())
                .build();
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
