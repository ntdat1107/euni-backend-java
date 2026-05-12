package com.euni.backend.service;

import com.euni.backend.dto.CourseDto;
import com.euni.backend.entity.Course;
import com.euni.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseDto createCourse(CourseDto dto) {
        if (courseRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã học phần đã tồn tại");
        }

        Course course = Course.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .credits(dto.getCredits())
                .description(dto.getDescription())
                .build();
        
        return toDto(courseRepository.save(course));
    }

    @Transactional
    public CourseDto updateCourse(UUID id, CourseDto dto) {
        Course course = courseRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học phần"));
        
        if (!course.getCode().equals(dto.getCode()) && courseRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã học phần đã tồn tại");
        }

        course.setName(dto.getName());
        course.setCode(dto.getCode());
        course.setCredits(dto.getCredits());
        course.setDescription(dto.getDescription());
        
        return toDto(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(UUID id) {
        Course course = courseRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học phần"));
        course.setDeleted(true);
        courseRepository.save(course);
    }

    private CourseDto toDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .credits(course.getCredits())
                .description(course.getDescription())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
