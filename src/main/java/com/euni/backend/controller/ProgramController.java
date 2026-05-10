package com.euni.backend.controller;

import com.euni.backend.dto.ProgramDto;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.euni.backend.dto.CourseAssignmentRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProgramController extends BaseController {

    private final ProgramService programService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProgramDto>>> getAllPrograms() {
        return ok(programService.getAllPrograms());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProgramDto>> createProgram(@RequestBody ProgramDto dto) {
        return ok(programService.createProgram(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgramDto>> updateProgram(@PathVariable UUID id, @RequestBody ProgramDto dto) {
        return ok(programService.updateProgram(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProgram(@PathVariable UUID id) {
        programService.deleteProgram(id);
        return ok(null);
    }

    @PostMapping("/{id}/courses")
    public ResponseEntity<ApiResponse<Void>> assignCourses(@PathVariable UUID id, @RequestBody CourseAssignmentRequest request) {
        programService.assignCourses(id, request.getCourseIds());
        return ok(null);
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<ApiResponse<List<UUID>>> getCoursesByProgram(@PathVariable UUID id) {
        return ok(programService.getCourseIdsByProgram(id));
    }
}
