package com.euni.backend.controller;

import com.euni.backend.dto.FacultyDto;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class FacultyController extends BaseController {

    private final FacultyService facultyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacultyDto>>> getAllFaculties() {
        return ok(facultyService.getAllFaculties());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FacultyDto>> createFaculty(@RequestBody FacultyDto dto) {
        return ok(facultyService.createFaculty(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDto>> updateFaculty(@PathVariable UUID id, @RequestBody FacultyDto dto) {
        return ok(facultyService.updateFaculty(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable UUID id) {
        facultyService.deleteFaculty(id);
        return ok(null);
    }
}
