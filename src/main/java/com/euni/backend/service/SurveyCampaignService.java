package com.euni.backend.service;

import com.euni.backend.dto.SurveyCampaignDto;
import com.euni.backend.dto.SurveyCampaignStepDto;
import com.euni.backend.entity.*;
import com.euni.backend.entity.enums.SurveyCampaignStatus;
import com.euni.backend.exception.ResourceNotFoundException;
import com.euni.backend.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.euni.backend.dto.request.SurveyCampaignRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyCampaignService {

    private final SurveyCampaignRepository campaignRepository;
    private final SurveyCampaignStepRepository stepRepository;
    private final ProgramRepository programRepository;
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<SurveyCampaignDto> getAll() {
        return campaignRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SurveyCampaignDto getById(UUID id) {
        SurveyCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", id));
        return convertToDto(campaign);
    }

    @Transactional
    public SurveyCampaignDto create(SurveyCampaignRequest request) {
        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.getProgramId()));

        WorkflowTemplate workflowTemplate = workflowTemplateRepository.findById(request.getWorkflowTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowTemplate", "id", request.getWorkflowTemplateId()));

        // Check duplicate active/uncompleted campaign for this program
        List<SurveyCampaignStatus> inactiveStatuses = List.of(
                SurveyCampaignStatus.COMPLETED,
                SurveyCampaignStatus.APPROVED,
                SurveyCampaignStatus.CANCELLED
        );
        Optional<SurveyCampaign> activeOpt = campaignRepository.findFirstByProgramIdAndStatusNotIn(request.getProgramId(), inactiveStatuses);
        if (activeOpt.isPresent()) {
            SurveyCampaign activeCamp = activeOpt.get();
            throw new IllegalArgumentException(
                    String.format("Chương trình đào tạo '%s' đang có một Đợt khảo sát chưa hoàn thành (Mã: %s - Tên: '%s'). Vui lòng hoàn thành hoặc Hủy đợt khảo sát hiện tại trước khi tạo đợt mới.",
                            program.getName(), activeCamp.getCode(), activeCamp.getName())
            );
        }

        if (request.getCode() != null && campaignRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã đợt khảo sát đã tồn tại: " + request.getCode());
        }

        SurveyCampaign campaign = SurveyCampaign.builder()
                .code(request.getCode() != null ? request.getCode() : "CAM-" + System.currentTimeMillis())
                .name(request.getName())
                .description(request.getDescription())
                .program(program)
                .workflowTemplate(workflowTemplate)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : SurveyCampaignStatus.ACTIVE)
                .steps(new ArrayList<>())
                .build();

        if (request.getSteps() != null) {
            for (SurveyCampaignRequest.SurveyCampaignStepRequest sReq : request.getSteps()) {
                String reqDocsJson = "[]";
                String configJson = "{}";
                try {
                    if (sReq.getRequiredDocuments() != null) {
                        reqDocsJson = objectMapper.writeValueAsString(sReq.getRequiredDocuments());
                    }
                    if (sReq.getConfiguration() != null) {
                        configJson = objectMapper.writeValueAsString(sReq.getConfiguration());
                    }
                } catch (Exception e) {
                    log.error("Error serializing step config/documents", e);
                }

                SurveyCampaignStep step = SurveyCampaignStep.builder()
                        .campaign(campaign)
                        .stepIndex(sReq.getStepIndex() != null ? sReq.getStepIndex() : 0)
                        .stepName(sReq.getStepName())
                        .deadline(sReq.getDeadline())
                        .requiredDocuments(reqDocsJson)
                        .configuration(configJson)
                        .status("DRAFT")
                        .build();

                campaign.getSteps().add(step);
            }
        }

        SurveyCampaign saved = campaignRepository.save(campaign);
        return convertToDto(saved);
    }

    @Transactional
    public SurveyCampaignDto update(UUID id, SurveyCampaignRequest request) {
        SurveyCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", id));

        if (request.getName() != null) campaign.setName(request.getName());
        if (request.getDescription() != null) campaign.setDescription(request.getDescription());
        if (request.getStartDate() != null) campaign.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) campaign.setEndDate(request.getEndDate());
        if (request.getStatus() != null) campaign.setStatus(request.getStatus());

        if (request.getProgramId() != null && (campaign.getProgram() == null || !request.getProgramId().equals(campaign.getProgram().getId()))) {
            Program program = programRepository.findById(request.getProgramId())
                    .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.getProgramId()));
            campaign.setProgram(program);
        }

        if (request.getWorkflowTemplateId() != null && (campaign.getWorkflowTemplate() == null || !request.getWorkflowTemplateId().equals(campaign.getWorkflowTemplate().getId()))) {
            WorkflowTemplate workflowTemplate = workflowTemplateRepository.findById(request.getWorkflowTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("WorkflowTemplate", "id", request.getWorkflowTemplateId()));
            campaign.setWorkflowTemplate(workflowTemplate);
        }

        if (request.getSteps() != null && !request.getSteps().isEmpty()) {
            for (SurveyCampaignRequest.SurveyCampaignStepRequest sReq : request.getSteps()) {
                if (sReq.getStepIndex() != null) {
                    SurveyCampaignStep step = campaign.getSteps().stream()
                            .filter(s -> s.getStepIndex().equals(sReq.getStepIndex()))
                            .findFirst()
                            .orElse(null);

                    if (step != null) {
                        if (sReq.getDeadline() != null) step.setDeadline(sReq.getDeadline());
                        try {
                            if (sReq.getRequiredDocuments() != null) {
                                step.setRequiredDocuments(objectMapper.writeValueAsString(sReq.getRequiredDocuments()));
                            }
                            if (sReq.getConfiguration() != null) {
                                step.setConfiguration(objectMapper.writeValueAsString(sReq.getConfiguration()));
                            }
                        } catch (Exception e) {
                            log.error("Error serializing step update config", e);
                        }
                    }
                }
            }
        }

        SurveyCampaign saved = campaignRepository.save(campaign);
        return convertToDto(saved);
    }

    @Transactional
    public SurveyCampaignDto cancelCampaign(UUID id) {
        // TODO: RBAC Permission Check (PERM_SURVEY_CAMPAIGN_CANCEL)
        SurveyCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", id));

        if (campaign.getStatus() == SurveyCampaignStatus.APPROVED) {
            throw new IllegalStateException("Không thể hủy Đợt khảo sát đã được phê duyệt chính thức.");
        }

        campaign.setStatus(SurveyCampaignStatus.CANCELLED);
        SurveyCampaign saved = campaignRepository.save(campaign);
        return convertToDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        cancelCampaign(id);
    }

    @Transactional(readOnly = true)
    public boolean checkCode(String code) {
        return campaignRepository.existsByCode(code);
    }

    @Transactional
    public SurveyCampaignStepDto saveStepData(UUID campaignId, UUID stepId, String resultData) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        if (!step.getCampaign().getId().equals(campaignId)) {
            throw new IllegalArgumentException("Step does not belong to the specified campaign");
        }

        try {
            JsonNode incomingNode = objectMapper.readTree(resultData);
            if (incomingNode != null && incomingNode.isObject() && incomingNode.has("courseId")) {
                String courseId = incomingNode.get("courseId").asText();
                ObjectNode courseNode = (ObjectNode) incomingNode;

                // Inject audit metadata for single course update
                courseNode.put("updatedAt", java.time.Instant.now().toString());
                String currentUser = "Quản trị viên hệ thống";
                try {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                        currentUser = auth.getName();
                    }
                } catch (Exception e) {
                    // Fallback username
                }
                courseNode.put("updatedBy", currentUser);

                // Merge into existing resultData map: { [courseId]: CourseSyllabusData }
                ObjectNode existingMap;
                if (step.getResultData() != null && !step.getResultData().isBlank()) {
                    try {
                        JsonNode existingNode = objectMapper.readTree(step.getResultData());
                        if (existingNode.isObject()) {
                            existingMap = (ObjectNode) existingNode;
                        } else {
                            existingMap = objectMapper.createObjectNode();
                        }
                    } catch (Exception e) {
                        existingMap = objectMapper.createObjectNode();
                    }
                } else {
                    existingMap = objectMapper.createObjectNode();
                }

                existingMap.set(courseId, courseNode);
                step.setResultData(objectMapper.writeValueAsString(existingMap));
            } else {
                step.setResultData(resultData);
            }
        } catch (Exception e) {
            log.warn("Failed to parse/merge resultData, saving as raw string: {}", e.getMessage());
            step.setResultData(resultData);
        }

        step.setStatus("COMPLETED");
        SurveyCampaignStep saved = stepRepository.save(step);

        return convertToStepDto(saved);
    }

    @Transactional
    public void approveCampaign(UUID id) {
        SurveyCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", id));

        campaign.setStatus(SurveyCampaignStatus.APPROVED);
        campaignRepository.save(campaign);

        // Trigger Sync-Back
        syncBackData(campaign);
    }

    private void syncBackData(SurveyCampaign campaign) {
        log.info("Starting sync-back for campaign: {}", campaign.getName());
        List<SurveyCampaignStep> steps = stepRepository.findByCampaignIdOrderByStepIndexAsc(campaign.getId());

        for (SurveyCampaignStep step : steps) {
            String screenCode = extractScreenCode(step.getConfiguration());
            if (screenCode == null) continue;

            switch (screenCode) {
                case "S2_PLO":
                    handleS2Sync(campaign.getProgram(), step.getResultData());
                    break;
                case "S5_CLO":
                    handleS5Sync(campaign.getProgram(), step.getResultData());
                    break;
                default:
                    log.debug("No sync logic for screen: {}", screenCode);
            }
        }
    }

    private void handleS2Sync(Program program, String resultData) {
        if (resultData == null || resultData.isEmpty()) return;
        
        program.setData(resultData);
        programRepository.save(program);
        log.info("Synced S2_PLO data for program: {}", program.getName());
    }

    private void handleS5Sync(Program program, String resultData) {
        if (resultData == null || resultData.isEmpty()) return;

        try {
            String courseCode = extractCourseCode(resultData); 
            if (courseCode != null) {
                Course course = courseRepository.findByCode(courseCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Course", "code", courseCode));
                
                ProgramCourse programCourse = programCourseRepository.findByProgramIdAndCourseId(program.getId(), course.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProgramCourse", "ids", "program:" + program.getId() + ", course:" + course.getId()));

                programCourse.setData(resultData);
                programCourseRepository.save(programCourse);
                log.info("Synced S5_CLO data for course: {} in program: {}", courseCode, program.getName());
            }
        } catch (Exception e) {
            log.error("Error syncing S5 data: {}", e.getMessage());
        }
    }

    private String extractScreenCode(String config) {
        try {
            if (config == null || config.isEmpty()) return null;
            return objectMapper.readTree(config).get("screenCode").asText();
        } catch (Exception e) {
            log.error("Error extracting screenCode: {}", e.getMessage());
            return null;
        }
    }

    private String extractCourseCode(String result) {
        try {
            if (result == null || result.isEmpty()) return null;
            return objectMapper.readTree(result).get("courseCode").asText();
        } catch (Exception e) {
            log.error("Error extracting courseCode: {}", e.getMessage());
            return null;
        }
    }

    private SurveyCampaignDto convertToDto(SurveyCampaign entity) {
        return SurveyCampaignDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .programId(entity.getProgram() != null ? entity.getProgram().getId() : null)
                .programName(entity.getProgram() != null ? entity.getProgram().getName() : null)
                .workflowTemplateId(entity.getWorkflowTemplate() != null ? entity.getWorkflowTemplate().getId() : null)
                .workflowTemplateName(entity.getWorkflowTemplate() != null ? entity.getWorkflowTemplate().getName() : null)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "DRAFT")
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .steps(entity.getSteps() != null ? entity.getSteps().stream().map(this::convertToStepDto).collect(Collectors.toList()) : List.of())
                .build();
    }

    private SurveyCampaignStepDto convertToStepDto(SurveyCampaignStep entity) {
        return SurveyCampaignStepDto.builder()
                .id(entity.getId())
                .stepIndex(entity.getStepIndex())
                .stepName(entity.getStepName())
                .deadline(entity.getDeadline())
                .requiredDocuments(entity.getRequiredDocuments())
                .configuration(entity.getConfiguration())
                .status(entity.getStatus())
                .resultData(entity.getResultData())
                .build();
    }
}

