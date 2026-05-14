package com.euni.backend.service;

import com.euni.backend.dto.response.WorkflowStepDefinitionResponse;
import com.euni.backend.entity.WorkflowStepDefinition;
import com.euni.backend.repository.WorkflowStepDefinitionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowStepDefinitionService {
    private final WorkflowStepDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<WorkflowStepDefinitionResponse> getAllDefinitions() {
        return repository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkflowStepDefinitionResponse> getByWorkflowType(String workflowType) {
        return repository.findByWorkflowType(workflowType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private WorkflowStepDefinitionResponse mapToResponse(WorkflowStepDefinition entity) {
        List<String> docs = new ArrayList<>();
        try {
            if (entity.getRequiredDocuments() != null && !entity.getRequiredDocuments().isEmpty()) {
                docs = Arrays.asList(objectMapper.readValue(entity.getRequiredDocuments(), String[].class));
            }
        } catch (JsonProcessingException e) {
            log.error("Error deserializing documents for step: {}", entity.getStepCode(), e);
        }

        return WorkflowStepDefinitionResponse.builder()
                .id(entity.getId())
                .workflowType(entity.getWorkflowType())
                .stepCode(entity.getStepCode())
                .stepName(entity.getStepName())
                .type(entity.getType())
                .requiredDocuments(docs)
                .build();
    }
}
