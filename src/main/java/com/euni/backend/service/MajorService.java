package com.euni.backend.service;

import com.euni.backend.dto.MajorDto;
import com.euni.backend.entity.Faculty;
import com.euni.backend.entity.Major;
import com.euni.backend.repository.FacultyRepository;
import com.euni.backend.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;
    private final FacultyRepository facultyRepository;

    @Transactional(readOnly = true)
    public List<MajorDto> getAllMajors() {
        return majorRepository.findAll().stream()
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
            faculty = facultyRepository.findById(dto.getFacultyId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));
        }

        Major major = Major.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .faculty(faculty)
                .build();
        
        return toDto(majorRepository.save(major));
    }

    @Transactional
    public MajorDto updateMajor(UUID id, MajorDto dto) {
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học"));
        
        if (!major.getCode().equals(dto.getCode()) && majorRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã ngành đã tồn tại");
        }

        Faculty faculty = null;
        if (dto.getFacultyId() != null) {
            faculty = facultyRepository.findById(dto.getFacultyId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));
        }

        major.setName(dto.getName());
        major.setCode(dto.getCode());
        major.setDescription(dto.getDescription());
        major.setFaculty(faculty);
        
        return toDto(majorRepository.save(major));
    }

    @Transactional
    public void deleteMajor(UUID id) {
        if (!majorRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy ngành học");
        }
        majorRepository.deleteById(id);
    }

    private MajorDto toDto(Major major) {
        return MajorDto.builder()
                .id(major.getId())
                .name(major.getName())
                .code(major.getCode())
                .description(major.getDescription())
                .facultyId(major.getFaculty() != null ? major.getFaculty().getId() : null)
                .facultyName(major.getFaculty() != null ? major.getFaculty().getName() : null)
                .programCount(major.getPrograms() != null ? major.getPrograms().size() : 0)
                .createdAt(major.getCreatedAt())
                .updatedAt(major.getUpdatedAt())
                .build();
    }
}
