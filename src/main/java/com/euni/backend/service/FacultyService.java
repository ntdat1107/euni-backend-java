package com.euni.backend.service;

import com.euni.backend.dto.FacultyDto;
import com.euni.backend.entity.Faculty;
import com.euni.backend.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;

    @Transactional(readOnly = true)
    public List<FacultyDto> getAllFaculties() {
        return facultyRepository.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FacultyDto createFaculty(FacultyDto dto) {
        if (facultyRepository.findByCode(dto.getCode()).isPresent()) {
            throw new RuntimeException("Mã khoa đã tồn tại");
        }
        Faculty faculty = Faculty.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .build();
        return toDto(facultyRepository.save(faculty));
    }

    @Transactional
    public FacultyDto updateFaculty(UUID id, FacultyDto dto) {
        Faculty faculty = facultyRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));
        
        facultyRepository.findByCode(dto.getCode()).ifPresent(f -> {
            if (!f.getId().equals(id)) {
                throw new RuntimeException("Mã khoa đã tồn tại");
            }
        });

        faculty.setName(dto.getName());
        faculty.setCode(dto.getCode());
        faculty.setDescription(dto.getDescription());
        
        return toDto(facultyRepository.save(faculty));
    }

    @Transactional
    public void deleteFaculty(UUID id) {
        Faculty faculty = facultyRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));
        faculty.setDeleted(true);
        facultyRepository.save(faculty);
    }

    private FacultyDto toDto(Faculty faculty) {
        return FacultyDto.builder()
                .id(faculty.getId())
                .name(faculty.getName())
                .code(faculty.getCode())
                .description(faculty.getDescription())
                .build();
    }
}
