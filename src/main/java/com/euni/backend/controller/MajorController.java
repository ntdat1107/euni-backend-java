package com.euni.backend.controller;

import com.euni.backend.dto.MajorDto;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/majors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class MajorController extends BaseController {

    private final MajorService majorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MajorDto>>> getAllMajors() {
        return ok(majorService.getAllMajors());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MajorDto>> createMajor(@RequestBody MajorDto dto) {
        return ok(majorService.createMajor(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MajorDto>> updateMajor(@PathVariable UUID id, @RequestBody MajorDto dto) {
        return ok(majorService.updateMajor(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMajor(@PathVariable UUID id) {
        majorService.deleteMajor(id);
        return ok(null);
    }
}
