package com.euni.backend.service;

import com.euni.backend.dto.SurveyCampaignDto;
import com.euni.backend.dto.SurveyCampaignStepDto;
import com.euni.backend.entity.*;
import com.euni.backend.entity.enums.SurveyCampaignStatus;
import com.euni.backend.exception.ResourceNotFoundException;
import com.euni.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyCampaignService {

    private final SurveyCampaignRepository campaignRepository;
    private final SurveyCampaignStepRepository stepRepository;
    private final ProgramRepository programRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final CourseRepository courseRepository;

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
    public SurveyCampaignStepDto saveStepData(UUID campaignId, UUID stepId, String resultData) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        if (!step.getCampaign().getId().equals(campaignId)) {
            throw new IllegalArgumentException("Step does not belong to the specified campaign");
        }

        step.setResultData(resultData);
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

    private final ObjectMapper objectMapper;

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
                .programId(entity.getProgram().getId())
                .programName(entity.getProgram().getName())
                .workflowTemplateId(entity.getWorkflowTemplate().getId())
                .workflowTemplateName(entity.getWorkflowTemplate().getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .steps(entity.getSteps().stream().map(this::convertToStepDto).collect(Collectors.toList()))
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
