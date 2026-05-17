package com.euni.backend.controller;

import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.dto.response.WorkflowStepDefinitionResponse;
import com.euni.backend.service.WorkflowStepDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow-definitions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class WorkflowStepDefinitionController extends BaseController {
    private final WorkflowStepDefinitionService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkflowStepDefinitionResponse>>> getAll() {
        return ok(service.getAllDefinitions());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<WorkflowStepDefinitionResponse>>> getByType(@PathVariable String type) {
        return ok(service.getByWorkflowType(type));
    }
}
