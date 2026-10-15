package com.euni.backend.service;

import com.euni.backend.dto.MajorDto;
import com.euni.backend.entity.Department;
import com.euni.backend.entity.Faculty;
import com.euni.backend.entity.Major;
import com.euni.backend.repository.DepartmentRepository;
import com.euni.backend.repository.FacultyRepository;
import com.euni.backend.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<MajorDto> getAllMajors() {
        return majorRepository.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MajorDto createMajor(MajorDto dto) {
        if (majorRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã ngành đã tồn tại");
        }
        
        Faculty faculty = null;
        if (dto.getFacultyId() != null) {
            faculty = facultyRepository.findActiveById(dto.getFacultyId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));
        }

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findActiveById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ môn"));
        }

        Major major = Major.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .faculty(faculty)
                .department(department)
                .build();
        
        return toDto(majorRepository.save(major));
    }

    @Transactional
    public MajorDto updateMajor(Long id, MajorDto dto) {
        Major major = majorRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học"));
        
        if (!major.getCode().equals(dto.getCode()) && majorRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã ngành đã tồn tại");
        }

        Faculty faculty = null;
        if (dto.getFacultyId() != null) {
            faculty = facultyRepository.findActiveById(dto.getFacultyId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));
        }

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findActiveById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ môn"));
        }

        major.setName(dto.getName());
        major.setCode(dto.getCode());
        major.setDescription(dto.getDescription());
        major.setFaculty(faculty);
        major.setDepartment(department);
        
        return toDto(majorRepository.save(major));
    }

    @Transactional
    public void deleteMajor(Long id) {
        Major major = majorRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học"));
        major.setDeleted(true);
        majorRepository.save(major);
    }

    private MajorDto toDto(Major major) {
        return MajorDto.builder()
                .id(major.getId())
                .name(major.getName())
                .code(major.getCode())
                .description(major.getDescription())
                .facultyId(major.getFaculty() != null ? major.getFaculty().getId() : null)
                .facultyName(major.getFaculty() != null ? major.getFaculty().getName() : null)
                .departmentId(major.getDepartment() != null ? major.getDepartment().getId() : null)
                .departmentName(major.getDepartment() != null ? major.getDepartment().getName() : null)
                .programCount(major.getPrograms() != null ? major.getPrograms().size() : 0)
                .createdAt(major.getCreatedAt())
                .updatedAt(major.getUpdatedAt())
                .build();
    }
}
