package com.euni.backend.service;

import com.euni.backend.dto.SurveyCampaignDto;
import com.euni.backend.dto.SurveyCampaignStepDto;
import com.euni.backend.entity.*;
import com.euni.backend.entity.enums.DomainType;
import com.euni.backend.entity.enums.ProgramStatus;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final SyllabusRepository syllabusRepository;
    private final ProgramService programService;
    private final WorkflowTemplateService workflowTemplateService;
    private final ProgramObjectiveRepository programObjectiveRepository;
    private final ProgramLearningOutcomeRepository programLearningOutcomeRepository;
    private final PerformanceIndicatorRepository performanceIndicatorRepository;
    private final ObjectivePiMapRepository objectivePiMapRepository;
    private final CourseLearningOutcomeRepository courseLearningOutcomeRepository;
    private final CloPiMapRepository cloPiMapRepository;
    private final CoursePiMapRepository coursePiMapRepository;

    @Transactional(readOnly = true)
    public List<SurveyCampaignDto> getAll() {
        return campaignRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SurveyCampaignDto getById(Long id) {
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

        if (program.getStatus() != ProgramStatus.DRAFT) {
            throw new IllegalArgumentException(
                    String.format("Chỉ được tạo đợt cho Chương trình đào tạo ở trạng thái Bản nháp (DRAFT). Chương trình '%s' hiện đang ở trạng thái '%s'.",
                            program.getName(), program.getStatus())
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
                .status(request.getStatus() != null ? request.getStatus() : SurveyCampaignStatus.IN_PROGRESS)
                .steps(new ArrayList<>())
                .build();

        boolean hasStep5 = false;
        if (request.getSteps() != null) {
            for (SurveyCampaignRequest.SurveyCampaignStepRequest sReq : request.getSteps()) {
                String reqDocsJson = "[]";
                String configJson = "{}";
                try {
                    if (sReq.getRequiredDocuments() != null) {
                        if (sReq.getRequiredDocuments() instanceof String) {
                            reqDocsJson = (String) sReq.getRequiredDocuments();
                        } else {
                            reqDocsJson = objectMapper.writeValueAsString(sReq.getRequiredDocuments());
                        }
                    }
                    if (sReq.getConfiguration() != null) {
                        if (sReq.getConfiguration() instanceof String) {
                            configJson = (String) sReq.getConfiguration();
                        } else {
                            configJson = objectMapper.writeValueAsString(sReq.getConfiguration());
                        }
                        String screenCode = extractScreenCode(configJson);
                        if ("S5_CLO".equals(screenCode)) {
                            hasStep5 = true;
                        }
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

        // Cập nhật trạng thái Chương trình Đào tạo sang REVIEWING (Đang xây dựng/rà soát)
        program.setStatus(ProgramStatus.REVIEWING);
        programRepository.save(program);

        // If campaign workflow contains Step 5 (S5_CLO), ensure syllabuses exist for all program courses
        for (SurveyCampaignStep step : saved.getSteps()) {
            String screenCode = extractScreenCode(step.getConfiguration());
            if ("S5_CLO".equals(screenCode)) {
                List<ProgramCourse> programCourses = programCourseRepository.findAllByProgramId(program.getId());
                for (ProgramCourse pc : programCourses) {
                    if (syllabusRepository.findByProgramCourseIdAndDeletedFalse(pc.getId()).isEmpty()) {
                        Syllabus s = Syllabus.builder()
                                .programCourse(pc)
                                .version("1.0")
                                .status("DRAFT")
                                .teachingMethods("Thuyết giảng kết hợp thảo luận nhóm và thực hành.")
                                .studentDuties("Tham gia học tập đầy đủ, hoàn thành đồ án môn học.")
                                .isMaster(true)
                                .build();
                        syllabusRepository.save(s);
                    }
                }
            }
        }

        return convertToDto(saved);
    }

    @Transactional
    public SurveyCampaignDto update(Long id, SurveyCampaignRequest request) {
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
                                Object docsObj = sReq.getRequiredDocuments();
                                if (docsObj instanceof String) {
                                    step.setRequiredDocuments((String) docsObj);
                                } else {
                                    step.setRequiredDocuments(objectMapper.writeValueAsString(docsObj));
                                }
                            }
                            if (sReq.getConfiguration() != null) {
                                Object configObj = sReq.getConfiguration();
                                if (configObj instanceof String) {
                                    step.setConfiguration((String) configObj);
                                } else {
                                    step.setConfiguration(objectMapper.writeValueAsString(configObj));
                                }
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
    public SurveyCampaignDto cancelCampaign(Long id) {
        // TODO: RBAC Permission Check (PERM_SURVEY_CAMPAIGN_CANCEL)
        SurveyCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", id));

        if (campaign.getStatus() == SurveyCampaignStatus.APPROVED) {
            throw new IllegalStateException("Không thể hủy Đợt khảo sát đã được phê duyệt chính thức.");
        }

        campaign.setStatus(SurveyCampaignStatus.CANCELLED);
        if (campaign.getProgram() != null && campaign.getProgram().getStatus() == ProgramStatus.REVIEWING) {
            campaign.getProgram().setStatus(ProgramStatus.DRAFT);
            programRepository.save(campaign.getProgram());
        }
        SurveyCampaign saved = campaignRepository.save(campaign);
        return convertToDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        cancelCampaign(id);
    }

    @Transactional(readOnly = true)
    public boolean checkCode(String code) {
        return campaignRepository.existsByCode(code);
    }

    @Transactional
    public SurveyCampaignStepDto saveStepData(Long campaignId, Long stepId, String resultData) {
        return saveStepData(campaignId, stepId, resultData, "COMPLETED", null);
    }

    @Transactional
    public SurveyCampaignStepDto saveStepData(Long campaignId, Long stepId, String resultData, String status) {
        return saveStepData(campaignId, stepId, resultData, status, null);
    }

    @Transactional
    public SurveyCampaignStepDto saveStepData(Long campaignId, Long stepId, String resultData, String status, String reviewNote) {
        SurveyCampaignStep step = stepRepository.findById(stepId).orElse(null);
        if (step == null || !step.getCampaign().getId().equals(campaignId)) {
            List<SurveyCampaignStep> steps = stepRepository.findByCampaignIdOrderByStepIndexAsc(campaignId);
            step = steps.stream()
                    .filter(s -> s.getStepIndex() != null && s.getStepIndex().equals(stepId.intValue()))
                    .findFirst()
                    .orElse(null);
            if (step == null) {
                throw new ResourceNotFoundException("SurveyCampaignStep", "id", stepId);
            }
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

        if (status != null && !status.isBlank()) {
            step.setStatus(status.toUpperCase());
        } else {
            step.setStatus("COMPLETED");
        }

        if ("UNDER_REVIEW".equalsIgnoreCase(step.getStatus())) {
            archiveStepReviewNote(step);
        } else if (reviewNote != null) {
            try {
                ObjectNode configNode;
                if (step.getConfiguration() != null && !step.getConfiguration().isBlank()) {
                    JsonNode parsed = objectMapper.readTree(step.getConfiguration());
                    configNode = parsed.isObject() ? (ObjectNode) parsed : objectMapper.createObjectNode();
                } else {
                    configNode = objectMapper.createObjectNode();
                }
                configNode.put("reviewNote", reviewNote);
                configNode.put("reviewedAt", java.time.Instant.now().toString());
                String currentUser = "Hệ thống";
                try {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                        currentUser = auth.getName();
                    }
                } catch (Exception ignored) {}
                configNode.put("reviewedBy", currentUser);
                step.setConfiguration(objectMapper.writeValueAsString(configNode));
            } catch (Exception e) {
                log.warn("Error updating reviewNote in step configuration", e);
            }
        }

        SurveyCampaignStep saved = stepRepository.save(step);

        if ("COMPLETED".equalsIgnoreCase(saved.getStatus())) {
            String screenCode = extractScreenCode(saved.getConfiguration());
            if ("S2_PLO".equals(screenCode) && saved.getCampaign() != null) {
                handleS2Sync(saved.getCampaign().getProgram(), saved.getResultData());
            } else if ("S6_MATRIX".equals(screenCode) && saved.getCampaign() != null) {
                handleS6Sync(saved.getCampaign().getProgram(), saved.getResultData());
            }
        }

        return convertToStepDto(saved);
    }

    @Transactional
    public void approveCampaign(Long id) {
        SurveyCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", id));

        // Kiểm tra tất cả các bước phải đạt trạng thái COMPLETED
        List<SurveyCampaignStep> steps = stepRepository.findByCampaignIdOrderByStepIndexAsc(campaign.getId());
        boolean allStepsCompleted = !steps.isEmpty() && steps.stream()
                .allMatch(s -> "COMPLETED".equalsIgnoreCase(s.getStatus()));
        if (!allStepsCompleted) {
            throw new IllegalStateException("Tất cả các bước trong quy trình phải hoàn thành (COMPLETED) trước khi hoàn tất quy trình.");
        }

        campaign.setStatus(SurveyCampaignStatus.APPROVED);
        if (campaign.getProgram() != null) {
            campaign.getProgram().setStatus(ProgramStatus.ACTIVE);
            programRepository.save(campaign.getProgram());
        }
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
                    handleS5Sync(campaign.getId(), campaign.getProgram(), step.getResultData());
                    break;
                case "S6_MATRIX":
                    handleS6Sync(campaign.getProgram(), step.getResultData());
                    break;
                default:
                    log.debug("No sync logic for screen: {}", screenCode);
            }
        }
    }

    @Transactional
    public void handleS2Sync(Program program, String resultData) {
        if (program == null || resultData == null || resultData.isBlank()) return;
        try {
            JsonNode rootNode = objectMapper.readTree(resultData);
            if (rootNode == null || !rootNode.isObject()) return;

            log.info("Starting S2_PLO sync-back to relational tables for program: {} (id: {})", program.getName(), program.getId());

            // 0. Xóa trước toàn bộ mapping cũ trong objective_pi_maps của CTĐT này
            objectivePiMapRepository.deleteAllByProgramId(program.getId());

            // 1. Sync POs -> ProgramObjective
            JsonNode posNode = rootNode.has("pos") ? rootNode.get("pos") : rootNode.get("peos");
            List<ProgramObjective> savedPos = new ArrayList<>();
            if (posNode != null && posNode.isArray()) {
                int poOrder = 1;
                for (JsonNode poNode : posNode) {
                    String content = poNode.has("content") ? poNode.get("content").asText("") : "";
                    if (content.isBlank()) continue;

                    String code = poNode.has("code") && !poNode.get("code").asText().isBlank()
                            ? poNode.get("code").asText()
                            : "PO" + poOrder;

                    final int currentOrder = poOrder;
                    ProgramObjective po = programObjectiveRepository.findByProgramIdAndCode(program.getId(), code)
                            .orElseGet(() -> ProgramObjective.builder().program(program).code(code).build());
                    po.setContent(content);
                    po.setOrderIndex(currentOrder);
                    po.setDeleted(false);
                    savedPos.add(programObjectiveRepository.save(po));
                    poOrder++;
                }

                // Delete obsolete POs for this program
                List<ProgramObjective> allPos = programObjectiveRepository.findAllByProgramId(program.getId());
                for (ProgramObjective oldPo : allPos) {
                    if (savedPos.stream().noneMatch(p -> p.getId().equals(oldPo.getId()))) {
                        objectivePiMapRepository.deleteAllByObjectiveId(oldPo.getId());
                        programObjectiveRepository.delete(oldPo);
                    }
                }
            }

            // 2. Sync PLOs & PIs -> ProgramLearningOutcome & PerformanceIndicator
            JsonNode plosNode = rootNode.get("plos");
            List<ProgramLearningOutcome> savedPlos = new ArrayList<>();
            Map<Long, List<PerformanceIndicator>> pisByPloId = new HashMap<>();

            if (plosNode != null && plosNode.isArray()) {
                int ploOrder = 1;
                for (JsonNode ploNode : plosNode) {
                    String ploContent = ploNode.has("content") ? ploNode.get("content").asText("") : "";
                    if (ploContent.isBlank()) continue;

                    String ploCode = ploNode.has("code") && !ploNode.get("code").asText().isBlank()
                            ? ploNode.get("code").asText()
                            : "PLO" + ploOrder;

                    DomainType domain = DomainType.KNOWLEDGE;
                    if (ploNode.has("domain")) {
                        String domainStr = ploNode.get("domain").asText().toUpperCase();
                        try {
                            domain = DomainType.valueOf(domainStr);
                        } catch (IllegalArgumentException e) {
                            if (domainStr.contains("SKILL") || domainStr.contains("KỸ NĂNG")) {
                                domain = DomainType.SKILL;
                            } else if (domainStr.contains("AUTONOMY") || domainStr.contains("NĂNG LỰC") || domainStr.contains("TỰ CHỦ")) {
                                domain = DomainType.AUTONOMY;
                            } else if (domainStr.contains("ATTITUDE") || domainStr.contains("PHẨM CHẤT") || domainStr.contains("THÁI ĐỘ")) {
                                domain = DomainType.ATTITUDE;
                            } else {
                                domain = DomainType.KNOWLEDGE;
                            }
                        }
                    }

                    final int currentPloOrder = ploOrder;
                    ProgramLearningOutcome plo = programLearningOutcomeRepository.findByProgramIdAndCode(program.getId(), ploCode)
                            .orElseGet(() -> ProgramLearningOutcome.builder().program(program).code(ploCode).build());
                    plo.setContent(ploContent);
                    plo.setName(ploCode);
                    plo.setDomain(domain);
                    plo.setOrderIndex(currentPloOrder);
                    plo.setDeleted(false);
                    ProgramLearningOutcome savedPlo = programLearningOutcomeRepository.save(plo);
                    savedPlos.add(savedPlo);

                    // Sync PIs for this PLO
                    JsonNode pisNode = ploNode.get("pis");
                    List<PerformanceIndicator> savedPisForPlo = new ArrayList<>();
                    if (pisNode != null && pisNode.isArray()) {
                        int piOrder = 1;
                        for (JsonNode piNode : pisNode) {
                            String piContent = piNode.has("content") ? piNode.get("content").asText("") : "";
                            if (piContent.isBlank()) continue;

                            String piCode = piNode.has("code") && !piNode.get("code").asText().isBlank()
                                    ? piNode.get("code").asText()
                                    : "PI" + currentPloOrder + "." + piOrder;

                            final int currentPiOrder = piOrder;
                            PerformanceIndicator pi = performanceIndicatorRepository.findByOutcomeIdAndCode(savedPlo.getId(), piCode)
                                    .orElseGet(() -> PerformanceIndicator.builder().outcome(savedPlo).code(piCode).build());
                            pi.setContent(piContent);
                            pi.setOrderIndex(currentPiOrder);
                            pi.setDeleted(false);
                            PerformanceIndicator savedPi = performanceIndicatorRepository.save(pi);
                            savedPisForPlo.add(savedPi);
                            piOrder++;
                        }
                    }

                    // Delete obsolete PIs for this PLO
                    List<PerformanceIndicator> currentPis = performanceIndicatorRepository.findAllByOutcomeId(savedPlo.getId());
                    for (PerformanceIndicator oldPi : currentPis) {
                        if (savedPisForPlo.stream().noneMatch(p -> p.getId().equals(oldPi.getId()))) {
                            objectivePiMapRepository.deleteAllByPerformanceIndicatorId(oldPi.getId());
                            coursePiMapRepository.deleteAllByPerformanceIndicatorId(oldPi.getId());
                            cloPiMapRepository.deleteAllByPerformanceIndicatorId(oldPi.getId());
                            performanceIndicatorRepository.delete(oldPi);
                        }
                    }

                    pisByPloId.put(savedPlo.getId(), savedPisForPlo);
                    ploOrder++;
                }

                // Delete obsolete PLOs for this program
                List<ProgramLearningOutcome> allCurrentPlos = programLearningOutcomeRepository.findAllByProgramId(program.getId());
                for (ProgramLearningOutcome oldPlo : allCurrentPlos) {
                    if (savedPlos.stream().noneMatch(p -> p.getId().equals(oldPlo.getId()))) {
                        List<PerformanceIndicator> oldPis = performanceIndicatorRepository.findAllByOutcomeId(oldPlo.getId());
                        for (PerformanceIndicator oldPi : oldPis) {
                            objectivePiMapRepository.deleteAllByPerformanceIndicatorId(oldPi.getId());
                            coursePiMapRepository.deleteAllByPerformanceIndicatorId(oldPi.getId());
                            cloPiMapRepository.deleteAllByPerformanceIndicatorId(oldPi.getId());
                            performanceIndicatorRepository.delete(oldPi);
                        }
                        programLearningOutcomeRepository.delete(oldPlo);
                    }
                }
            }

            // 3. Sync Matrix -> ObjectivePiMap
            JsonNode matrixNode = rootNode.get("matrix");
            if (matrixNode != null && matrixNode.isArray()) {
                for (int peoIdx = 0; peoIdx < Math.min(matrixNode.size(), savedPos.size()); peoIdx++) {
                    JsonNode ploRow = matrixNode.get(peoIdx);
                    if (ploRow == null || !ploRow.isArray()) continue;
                    ProgramObjective currentPo = savedPos.get(peoIdx);

                    for (int ploIdx = 0; ploIdx < Math.min(ploRow.size(), savedPlos.size()); ploIdx++) {
                        JsonNode piRow = ploRow.get(ploIdx);
                        if (piRow == null || !piRow.isArray()) continue;
                        ProgramLearningOutcome currentPlo = savedPlos.get(ploIdx);
                        List<PerformanceIndicator> ploPis = pisByPloId.getOrDefault(currentPlo.getId(), List.of());

                        for (int piIdx = 0; piIdx < Math.min(piRow.size(), ploPis.size()); piIdx++) {
                            if (piRow.get(piIdx).asBoolean()) {
                                PerformanceIndicator currentPi = ploPis.get(piIdx);
                                ObjectivePiMap map = ObjectivePiMap.builder()
                                        .objective(currentPo)
                                        .performanceIndicator(currentPi)
                                        .build();
                                objectivePiMapRepository.save(map);
                            }
                        }
                    }
                }
            }

            log.info("Successfully synced S2_PLO data: {} POs, {} PLOs for program: {}",
                    savedPos.size(), savedPlos.size(), program.getName());
        } catch (Exception e) {
            log.error("Failed to sync S2_PLO data for program: {}", program.getName(), e);
            throw new RuntimeException("Failed to sync S2_PLO data: " + e.getMessage(), e);
        }
    }

    private void handleS5Sync(Long campaignId, Program program, String resultData) {
        log.info("Handled S5_CLO direct workflow event for program: {}", program.getName());
    }

    @Transactional
    public void handleS6Sync(Program program, String resultData) {
        if (program == null || resultData == null || resultData.isBlank()) return;
        try {
            JsonNode root = objectMapper.readTree(resultData);
            JsonNode mappingNode = root.has("mapping") ? root.get("mapping") : (root.has("matrix") ? root.get("matrix") : null);
            if (mappingNode == null || !mappingNode.isObject()) return;

            log.info("Starting S6_MATRIX sync-back for program: {}", program.getName());

            List<ProgramCourse> pcs = programCourseRepository.findAllByProgramId(program.getId());
            Map<Long, ProgramCourse> pcByCourseId = pcs.stream()
                    .collect(Collectors.toMap(pc -> pc.getCourse().getId(), pc -> pc, (a, b) -> a));
            Map<Long, ProgramCourse> pcById = pcs.stream()
                    .collect(Collectors.toMap(ProgramCourse::getId, pc -> pc, (a, b) -> a));

            List<PerformanceIndicator> allPis = performanceIndicatorRepository.findAllByProgramId(program.getId());
            Map<Long, PerformanceIndicator> piById = allPis.stream()
                    .collect(Collectors.toMap(PerformanceIndicator::getId, p -> p, (a, b) -> a));
            Map<String, PerformanceIndicator> piByCode = allPis.stream()
                    .collect(Collectors.toMap(PerformanceIndicator::getCode, p -> p, (a, b) -> a));

            Iterator<Map.Entry<String, JsonNode>> fields = mappingNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String rowKey = entry.getKey();
                JsonNode piMapNode = entry.getValue();
                if (!piMapNode.isObject()) continue;

                ProgramCourse pc = null;
                try {
                    Long numKey = Long.valueOf(rowKey);
                    pc = pcById.get(numKey);
                    if (pc == null) pc = pcByCourseId.get(numKey);
                } catch (Exception ignored) {}

                if (pc == null) continue;

                Iterator<Map.Entry<String, JsonNode>> piFields = piMapNode.fields();
                while (piFields.hasNext()) {
                    Map.Entry<String, JsonNode> piEntry = piFields.next();
                    String piKey = piEntry.getKey();
                    JsonNode valNode = piEntry.getValue();

                    PerformanceIndicator pi = null;
                    try {
                        Long piNum = Long.valueOf(piKey);
                        pi = piById.get(piNum);
                    } catch (Exception ignored) {}
                    if (pi == null) {
                        pi = piByCode.get(piKey);
                    }
                    if (pi == null) continue;

                    Integer level = null;
                    if (valNode.isInt()) {
                        level = valNode.asInt();
                    } else if (valNode.isTextual()) {
                        String txt = valNode.asText();
                        if ("I".equalsIgnoreCase(txt) || "1".equals(txt)) level = 1;
                        else if ("R".equalsIgnoreCase(txt) || "2".equals(txt)) level = 2;
                        else if ("M".equalsIgnoreCase(txt) || "3".equals(txt)) level = 3;
                        else if ("A".equalsIgnoreCase(txt) || "4".equals(txt)) level = 4;
                    }

                    final ProgramCourse targetPc = pc;
                    final PerformanceIndicator targetPi = pi;
                    Optional<CoursePiMap> existing = coursePiMapRepository.findByProgramCourseIdAndPiId(targetPc.getId(), targetPi.getId());
                    if (level == null || level <= 0) {
                        existing.ifPresent(coursePiMapRepository::delete);
                    } else {
                        CoursePiMap map = existing.orElseGet(() -> CoursePiMap.builder()
                                .programCourse(targetPc)
                                .performanceIndicator(targetPi)
                                .build());
                        map.setLevel(level);
                        coursePiMapRepository.save(map);
                    }
                }
            }
            log.info("Successfully synced S6_MATRIX for program: {}", program.getName());
        } catch (Exception e) {
            log.error("Failed to sync S6_MATRIX data", e);
        }
    }

    private void syncSyllabusDetails(Syllabus syllabus, String jsonStr, Long programId) {
        if (jsonStr == null || jsonStr.isBlank()) return;
        try {
            JsonNode root = objectMapper.readTree(jsonStr);
            if (root.has("courseDescription")) {
                syllabus.setCourseDescription(root.get("courseDescription").asText(""));
            }
            if (root.has("knowledgeObjective") || root.has("skillsObjective")) {
                Map<String, String> objMap = new HashMap<>();
                if (root.has("knowledgeObjective")) objMap.put("knowledge", root.get("knowledgeObjective").asText(""));
                if (root.has("skillsObjective")) objMap.put("skills", root.get("skillsObjective").asText(""));
                syllabus.setCourseObjectives(objectMapper.writeValueAsString(objMap));
            }
            if (root.has("facilitiesRequirements")) {
                syllabus.setFacilitiesRequirements(root.get("facilitiesRequirements").asText(""));
            }
            if (root.has("assessmentComponents")) {
                syllabus.setAssessmentScheme(objectMapper.writeValueAsString(root.get("assessmentComponents")));
            }
            if (root.has("materials")) {
                syllabus.setTextbooks(objectMapper.writeValueAsString(root.get("materials")));
            }

            if (syllabus.getId() != null && root.has("clos") && root.get("clos").isArray()) {
                List<PerformanceIndicator> allPis = performanceIndicatorRepository.findAllByProgramId(programId);
                Map<String, PerformanceIndicator> piByCode = allPis.stream()
                        .collect(Collectors.toMap(PerformanceIndicator::getCode, p -> p, (a, b) -> a));

                List<CourseLearningOutcome> savedClos = new ArrayList<>();
                int idx = 1;
                for (JsonNode cloNode : root.get("clos")) {
                    String code = cloNode.has("code") ? cloNode.get("code").asText("CLO" + idx) : "CLO" + idx;
                    String content = cloNode.has("content") ? cloNode.get("content").asText("") : "";
                    String bloom = cloNode.has("bloomLevel") ? cloNode.get("bloomLevel").asText("APPLY") : "APPLY";

                    final int currentOrder = idx;
                    CourseLearningOutcome clo = courseLearningOutcomeRepository
                            .findBySyllabusIdAndCodeAndDeletedFalse(syllabus.getId(), code)
                            .orElseGet(() -> CourseLearningOutcome.builder().syllabus(syllabus).code(code).build());
                    clo.setContent(content);
                    clo.setBloomLevel(bloom);
                    clo.setOrderIndex(currentOrder);
                    clo.setDeleted(false);
                    CourseLearningOutcome savedClo = courseLearningOutcomeRepository.save(clo);
                    savedClos.add(savedClo);

                    cloPiMapRepository.deleteAllByCloId(savedClo.getId());
                    if (cloNode.has("mappedPlos") && cloNode.get("mappedPlos").isArray()) {
                        for (JsonNode mappedPiNode : cloNode.get("mappedPlos")) {
                            String piCode = mappedPiNode.asText("");
                            PerformanceIndicator pi = piByCode.get(piCode);
                            if (pi != null) {
                                CloPiMap map = CloPiMap.builder()
                                        .clo(savedClo)
                                        .performanceIndicator(pi)
                                        .build();
                                cloPiMapRepository.save(map);
                            }
                        }
                    }
                    idx++;
                }

                List<CourseLearningOutcome> existingClos = courseLearningOutcomeRepository.findAllBySyllabusIdAndDeletedFalseOrderByOrderIndexAsc(syllabus.getId());
                for (CourseLearningOutcome oldClo : existingClos) {
                    if (savedClos.stream().noneMatch(c -> c.getId().equals(oldClo.getId()))) {
                        cloPiMapRepository.deleteAllByCloId(oldClo.getId());
                        courseLearningOutcomeRepository.delete(oldClo);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error syncing syllabus details and CLOs", e);
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

    private SurveyCampaignDto convertToDto(SurveyCampaign entity) {
        List<ProgramCourse> programCourses = entity.getProgram() != null
                ? programCourseRepository.findAllByProgramId(entity.getProgram().getId())
                : List.of();

        List<com.euni.backend.dto.SurveyCampaignCourseMetaDto> courseMetas = programCourses.stream()
                .map(pc -> {
                    Optional<Syllabus> sylOpt = syllabusRepository.findFirstByProgramCourseIdAndDeletedFalseOrderByVersionDesc(pc.getId());
                    String status = sylOpt.map(Syllabus::getStatus).orElse("DRAFT");
                    Course c = pc.getCourse();
                    return com.euni.backend.dto.SurveyCampaignCourseMetaDto.builder()
                            .id(pc.getId())
                            .stepId(null)
                            .courseId(c.getId())
                            .courseCode(c.getCode())
                            .courseName(c.getName())
                            .courseNameEn(c.getNameEn())
                            .credits(c.getCredits())
                            .theoryCredits(c.getTheoryCredits())
                            .practiceCredits(c.getPracticeCredits())
                            .selfStudyHours(c.getSelfStudyHours())
                            .semester(pc.getSemester())
                            .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());

        return SurveyCampaignDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .programId(entity.getProgram() != null ? entity.getProgram().getId() : null)
                .programName(entity.getProgram() != null ? entity.getProgram().getName() : null)
                .program(entity.getProgram() != null ? programService.toDto(entity.getProgram()) : null)
                .workflowTemplateId(entity.getWorkflowTemplate() != null ? entity.getWorkflowTemplate().getId() : null)
                .workflowTemplateName(entity.getWorkflowTemplate() != null ? entity.getWorkflowTemplate().getName() : null)
                .workflowTemplate(entity.getWorkflowTemplate() != null ? workflowTemplateService.getTemplateById(entity.getWorkflowTemplate().getId()) : null)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "DRAFT")
                .courses(courseMetas)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .steps(entity.getSteps() != null ? entity.getSteps().stream().map(this::convertToStepDto).collect(Collectors.toList()) : List.of())
                .build();
    }

    @Transactional(readOnly = true)
    public com.euni.backend.dto.SurveyCampaignCourseDetailDto getCampaignCourseDetail(Long campaignId, Long courseId, Long stepId) {
        SurveyCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", campaignId));

        ProgramCourse pc = programCourseRepository.findByProgramIdAndCourseId(campaign.getProgram().getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("ProgramCourse", "program/course", campaign.getProgram().getId() + "/" + courseId));

        Syllabus syllabus = syllabusRepository.findFirstByProgramCourseIdAndDeletedFalseOrderByVersionDesc(pc.getId())
                .orElseGet(() -> Syllabus.builder()
                        .programCourse(pc)
                        .version("1.0")
                        .status("DRAFT")
                        .build());

        String jsonOut = syllabus.getWeeklySchedule() != null ? syllabus.getWeeklySchedule() : "{}";
        Course c = pc.getCourse();

        return com.euni.backend.dto.SurveyCampaignCourseDetailDto.builder()
                .id(pc.getId())
                .campaignId(campaignId)
                .stepId(stepId)
                .courseId(c.getId())
                .courseCode(c.getCode())
                .courseName(c.getName())
                .courseNameEn(c.getNameEn())
                .credits(c.getCredits())
                .theoryCredits(c.getTheoryCredits())
                .practiceCredits(c.getPracticeCredits())
                .selfStudyHours(c.getSelfStudyHours())
                .semester(pc.getSemester())
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                .status(syllabus.getStatus())
                .syllabusData(jsonOut)
                .createdAt(syllabus.getCreatedAt())
                .updatedAt(syllabus.getUpdatedAt())
                .build();
    }

    @Transactional
    public com.euni.backend.dto.SurveyCampaignCourseDetailDto saveCampaignCourseDetail(Long campaignId, Long courseId, Map<String, Object> data) {
        SurveyCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", campaignId));

        ProgramCourse pc = programCourseRepository.findByProgramIdAndCourseId(campaign.getProgram().getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("ProgramCourse", "program/course", campaign.getProgram().getId() + "/" + courseId));

        Syllabus syllabus = syllabusRepository.findFirstByProgramCourseIdAndDeletedFalseOrderByVersionDesc(pc.getId())
                .orElseGet(() -> Syllabus.builder()
                        .programCourse(pc)
                        .version("1.0")
                        .isMaster(true)
                        .build());

        if (data.get("syllabusData") != null) {
            syllabus.setWeeklySchedule(data.get("syllabusData").toString());
        }
        if (data.get("status") != null) {
            syllabus.setStatus(data.get("status").toString());
        } else {
            syllabus.setStatus("COMPLETED");
        }

        Syllabus saved = syllabusRepository.save(syllabus);

        if (data.get("syllabusData") != null) {
            syncSyllabusDetails(saved, data.get("syllabusData").toString(), campaign.getProgram().getId());
        }

        Course c = pc.getCourse();
        String reviewNote = data.get("reviewNote") != null ? data.get("reviewNote").toString() : null;

        return com.euni.backend.dto.SurveyCampaignCourseDetailDto.builder()
                .id(pc.getId())
                .campaignId(campaignId)
                .stepId(data.get("stepId") != null ? Long.valueOf(data.get("stepId").toString()) : null)
                .courseId(c.getId())
                .courseCode(c.getCode())
                .courseName(c.getName())
                .courseNameEn(c.getNameEn())
                .credits(c.getCredits())
                .theoryCredits(c.getTheoryCredits())
                .practiceCredits(c.getPracticeCredits())
                .selfStudyHours(c.getSelfStudyHours())
                .semester(pc.getSemester())
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                .status(saved.getStatus())
                .reviewNote(reviewNote)
                .syllabusData(saved.getWeeklySchedule())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    private void archiveStepReviewNote(SurveyCampaignStep step) {
        if (step.getConfiguration() != null && !step.getConfiguration().isBlank()) {
            try {
                JsonNode parsed = objectMapper.readTree(step.getConfiguration());
                if (parsed.isObject()) {
                    ObjectNode configNode = (ObjectNode) parsed;
                    if (configNode.has("reviewNote") && !configNode.get("reviewNote").asText().isBlank()) {
                        configNode.put("previousReviewNote", configNode.get("reviewNote").asText());
                        if (configNode.has("reviewedBy")) {
                            configNode.put("previousReviewedBy", configNode.get("reviewedBy").asText());
                        }
                        if (configNode.has("reviewedAt")) {
                            configNode.put("previousReviewedAt", configNode.get("reviewedAt").asText());
                        }
                        configNode.remove("reviewNote");
                        configNode.remove("reviewedBy");
                        configNode.remove("reviewedAt");
                        step.setConfiguration(objectMapper.writeValueAsString(configNode));
                    }
                }
            } catch (Exception e) {
                log.warn("Error archiving review note in step config", e);
            }
        }
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
