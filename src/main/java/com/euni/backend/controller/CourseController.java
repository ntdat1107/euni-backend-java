package com.euni.backend.controller;

import com.euni.backend.dto.CourseDto;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController extends BaseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDto>>> getAllCourses() {
        return ok(courseService.getAllCourses());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(@RequestBody CourseDto dto) {
        return ok(courseService.createCourse(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(@PathVariable UUID id, @RequestBody CourseDto dto) {
        return ok(courseService.updateCourse(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ok(null);
    }
}
