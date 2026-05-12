package com.euni.backend.service;

import com.euni.backend.dto.request.SurveyCampaignRequest;
import com.euni.backend.dto.response.SurveyCampaignResponse;
import com.euni.backend.entity.*;
import com.euni.backend.entity.enums.SurveyCampaignStatus;
import com.euni.backend.exception.ResourceNotFoundException;
import com.euni.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyCampaignService {
    private final SurveyCampaignRepository campaignRepository;
    private final ProgramRepository programRepository;
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<SurveyCampaignResponse> getAllCampaigns() {
        return campaignRepository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SurveyCampaignResponse getCampaignById(UUID id) {
        SurveyCampaign campaign = campaignRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", id));
        return mapToResponse(campaign);
    }

    @Transactional
    public SurveyCampaignResponse createCampaign(SurveyCampaignRequest request) {
        Program program = programRepository.findActiveById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.getProgramId()));
        
        WorkflowTemplate template = workflowTemplateRepository.findActiveById(request.getWorkflowTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowTemplate", "id", request.getWorkflowTemplateId()));

        SurveyCampaign campaign = SurveyCampaign.builder()
                .code(request.getCode() != null ? request.getCode() : "CAM-" + System.currentTimeMillis())
                .name(request.getName())
                .description(request.getDescription())
                .program(program)
                .workflowTemplate(template)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : SurveyCampaignStatus.DRAFT)
                .build();

        if (request.getSteps() != null) {
            List<SurveyCampaignStep> steps = request.getSteps().stream()
                    .map(stepReq -> mapStepRequestToEntity(stepReq, campaign))
                    .collect(Collectors.toList());
            campaign.setSteps(steps);
        }

        return mapToResponse(campaignRepository.save(campaign));
    }

    @Transactional
    public SurveyCampaignResponse updateCampaign(UUID id, SurveyCampaignRequest request) {
        SurveyCampaign campaign = campaignRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", id));

        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            campaign.setStatus(request.getStatus());
        }

        // Update steps: For simplicity, replace all steps
        if (request.getSteps() != null) {
            campaign.getSteps().clear();
            List<SurveyCampaignStep> newSteps = request.getSteps().stream()
                    .map(stepReq -> mapStepRequestToEntity(stepReq, campaign))
                    .collect(Collectors.toList());
            campaign.getSteps().addAll(newSteps);
        }

        return mapToResponse(campaignRepository.save(campaign));
    }

    @Transactional
    public void deleteCampaign(UUID id) {
        SurveyCampaign campaign = campaignRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaign", "id", id));
        campaign.setDeleted(true);
        campaignRepository.save(campaign);
    }

    private SurveyCampaignStep mapStepRequestToEntity(SurveyCampaignRequest.SurveyCampaignStepRequest stepReq, SurveyCampaign campaign) {
        String documentsJson = "";
        String configJson = "";
        try {
            documentsJson = objectMapper.writeValueAsString(stepReq.getRequiredDocuments());
            configJson = objectMapper.writeValueAsString(stepReq.getConfiguration());
        } catch (JsonProcessingException e) {
            log.error("Error serializing step data for: {}", stepReq.getStepName(), e);
        }

        return SurveyCampaignStep.builder()
                .campaign(campaign)
                .stepIndex(stepReq.getStepIndex())
                .stepName(stepReq.getStepName())
                .deadline(stepReq.getDeadline())
                .documents(documentsJson)
                .configuration(configJson)
                .build();
    }

    private SurveyCampaignResponse mapToResponse(SurveyCampaign campaign) {
        return SurveyCampaignResponse.builder()
                .id(campaign.getId())
                .code(campaign.getCode())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .programId(campaign.getProgram().getId())
                .programName(campaign.getProgram().getName())
                .workflowTemplateId(campaign.getWorkflowTemplate().getId())
                .workflowTemplateName(campaign.getWorkflowTemplate().getName())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .status(campaign.getStatus())
                .createdAt(campaign.getCreatedAt())
                .steps(campaign.getSteps().stream()
                        .filter(step -> !step.isDeleted())
                        .map(this::mapStepToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private SurveyCampaignResponse.SurveyCampaignStepResponse mapStepToResponse(SurveyCampaignStep step) {
        List<String> docs = new ArrayList<>();
        Map<String, Object> config = new HashMap<>();
        try {
            if (step.getDocuments() != null && !step.getDocuments().isEmpty()) {
                docs = Arrays.asList(objectMapper.readValue(step.getDocuments(), String[].class));
            }
            if (step.getConfiguration() != null && !step.getConfiguration().isEmpty()) {
                config = objectMapper.readValue(step.getConfiguration(), Map.class);
            }
        } catch (JsonProcessingException e) {
            log.error("Error deserializing step data for: {}", step.getStepName(), e);
        }

        return SurveyCampaignResponse.SurveyCampaignStepResponse.builder()
                .id(step.getId())
                .stepIndex(step.getStepIndex())
                .stepName(step.getStepName())
                .deadline(step.getDeadline())
                .requiredDocuments(docs)
                .configuration(config)
                .build();
    }
}
