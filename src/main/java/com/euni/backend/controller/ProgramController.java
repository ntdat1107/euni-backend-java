package com.euni.backend.controller;

import com.euni.backend.dto.*;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.ProgramObeService;
import com.euni.backend.service.ProgramService;
import com.euni.backend.service.ProgramStructureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/programs", "/api/programs"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProgramController extends BaseController {

    private final ProgramService programService;
    private final ProgramStructureService programStructureService;
    private final ProgramObeService programObeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProgramDto>>> getAllPrograms() {
        return ok(programService.getAllPrograms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgramDto>> getProgramById(@PathVariable Long id) {
        return ok(programService.getProgramById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProgramDto>> createProgram(@RequestBody ProgramDto dto) {
        return ok(programService.createProgram(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgramDto>> updateProgram(@PathVariable Long id, @RequestBody ProgramDto dto) {
        return ok(programService.updateProgram(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
        return ok(null);
    }

    @PostMapping("/{id}/courses")
    public ResponseEntity<ApiResponse<Void>> assignCourses(@PathVariable Long id, @RequestBody CourseAssignmentRequest request) {
        programService.assignCoursesWithDetails(id, request);
        return ok(null);
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<ApiResponse<List<Long>>> getCoursesByProgram(@PathVariable Long id) {
        return ok(programService.getCourseIdsByProgram(id));
    }

    @GetMapping("/{id}/courses/detail")
    public ResponseEntity<ApiResponse<List<ProgramCourseDetailDto>>> getProgramCoursesDetail(@PathVariable Long id) {
        return ok(programService.getProgramCoursesDetail(id));
    }

    // --- Program Structures (Cây khối kiến thức) ---
    @GetMapping("/{id}/structures")
    public ResponseEntity<ApiResponse<List<ProgramStructureTreeResponse>>> getStructureTree(@PathVariable Long id) {
        return ok(programStructureService.getStructureTree(id));
    }

    @PostMapping("/{id}/structures")
    public ResponseEntity<ApiResponse<ProgramStructureTreeResponse>> createStructure(
            @PathVariable Long id,
            @Valid @RequestBody CreateStructureRequest request) {
        return ok(programStructureService.createStructure(id, request));
    }

    @PutMapping("/structures/{structureId}")
    public ResponseEntity<ApiResponse<ProgramStructureTreeResponse>> updateStructure(
            @PathVariable Long structureId,
            @Valid @RequestBody CreateStructureRequest request) {
        return ok(programStructureService.updateStructure(structureId, request));
    }

    @DeleteMapping("/structures/{structureId}")
    public ResponseEntity<ApiResponse<Void>> deleteStructure(@PathVariable Long structureId) {
        programStructureService.deleteStructure(structureId);
        return ok(null);
    }

    @PutMapping("/{id}/structures/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderStructures(
            @PathVariable Long id,
            @RequestBody List<Long> orderedIds) {
        programStructureService.reorderStructures(orderedIds);
        return ok(null);
    }

    @PostMapping("/structures/{structureId}/elective-groups")
    public ResponseEntity<ApiResponse<ProgramElectiveGroupDto>> addElectiveGroup(
            @PathVariable Long structureId,
            @RequestBody ProgramElectiveGroupDto dto) {
        return ok(programStructureService.addElectiveGroup(structureId, dto));
    }

    @PutMapping("/structures/elective-groups/{groupId}")
    public ResponseEntity<ApiResponse<ProgramElectiveGroupDto>> updateElectiveGroup(
            @PathVariable Long groupId,
            @RequestBody ProgramElectiveGroupDto dto) {
        return ok(programStructureService.updateElectiveGroup(groupId, dto));
    }

    @DeleteMapping("/structures/elective-groups/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteElectiveGroup(@PathVariable Long groupId) {
        programStructureService.deleteElectiveGroup(groupId);
        return ok(null);
    }

    // --- OBE & Matrices ---
    @GetMapping("/{id}/obe")
    public ResponseEntity<ApiResponse<ProgramObeMatrixResponse>> getProgramObe(@PathVariable Long id) {
        return ok(programObeService.getProgramObe(id));
    }

    @PutMapping("/{id}/obe/course-matrix")
    public ResponseEntity<ApiResponse<Void>> updateCoursePiMatrix(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCoursePiMapRequest request) {
        programObeService.updateCoursePiMap(request);
        return ok(null);
    }

    @PutMapping("/{id}/obe/course-matrix/batch")
    public ResponseEntity<ApiResponse<Void>> batchUpdateCoursePiMatrix(
            @PathVariable Long id,
            @RequestBody List<UpdateCoursePiMapRequest> requests) {
        programObeService.batchUpdateCoursePiMap(id, requests);
        return ok(null);
    }

    @PostMapping("/{id}/obe/auto-derive")
    public ResponseEntity<ApiResponse<ProgramObeMatrixResponse>> autoDeriveMatrixFromSyllabuses(
            @PathVariable Long id) {
        return ok(programObeService.autoDeriveMatrixFromSyllabuses(id));
    }
}
