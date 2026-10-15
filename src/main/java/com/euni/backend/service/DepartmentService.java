package com.euni.backend.service;

import com.euni.backend.dto.DepartmentDto;
import com.euni.backend.entity.Department;
import com.euni.backend.entity.Faculty;
import com.euni.backend.repository.DepartmentRepository;
import com.euni.backend.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getDepartmentsByFaculty(Long facultyId) {
        return departmentRepository.findByFacultyId(facultyId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (departmentRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã bộ môn đã tồn tại: " + dto.getCode());
        }

        Faculty faculty = null;
        if (dto.getFacultyId() != null) {
            faculty = facultyRepository.findActiveById(dto.getFacultyId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));
        }

        Department department = Department.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .faculty(faculty)
                .build();

        return toDto(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        Department department = departmentRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ môn: " + id));

        if (!department.getCode().equals(dto.getCode()) && departmentRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã bộ môn đã tồn tại: " + dto.getCode());
        }

        Faculty faculty = null;
        if (dto.getFacultyId() != null) {
            faculty = facultyRepository.findActiveById(dto.getFacultyId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));
        }

        department.setName(dto.getName());
        department.setCode(dto.getCode());
        department.setDescription(dto.getDescription());
        department.setFaculty(faculty);

        return toDto(departmentRepository.save(department));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ môn: " + id));
        department.setDeleted(true);
        departmentRepository.save(department);
    }

    public DepartmentDto toDto(Department dept) {
        if (dept == null) return null;
        return DepartmentDto.builder()
                .id(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .description(dept.getDescription())
                .facultyId(dept.getFaculty() != null ? dept.getFaculty().getId() : null)
                .facultyName(dept.getFaculty() != null ? dept.getFaculty().getName() : null)
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }
}
