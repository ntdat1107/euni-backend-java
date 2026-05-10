package com.euni.backend.service;

import com.euni.backend.dto.ProgramDto;
import com.euni.backend.entity.Major;
import com.euni.backend.entity.Program;
import com.euni.backend.entity.enums.ProgramStatus;
import com.euni.backend.entity.ProgramCourse;
import com.euni.backend.entity.Course;
import com.euni.backend.entity.history.ProgramHistory;
import com.euni.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final MajorRepository majorRepository;
    private final ProgramHistoryRepository programHistoryRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final CourseRepository courseRepository;
    private final ProgramCourseHistoryRepository programCourseHistoryRepository;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ProgramDto> getAllPrograms() {
        return programRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProgramDto createProgram(ProgramDto dto) {
        if (programRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã chương trình đã tồn tại");
        }
        
        Major major = majorRepository.findById(dto.getMajorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học"));

        Program program = Program.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .major(major)
                .status(dto.getStatus() != null ? dto.getStatus() : ProgramStatus.DRAFT)
                .generalObjective(dto.getGeneralObjective())
                .specificObjectives(dto.getSpecificObjectives())
                .learningOutcomes(dto.getLearningOutcomes())
                .build();
        
        return toDto(programRepository.save(program));
    }

    @Transactional
    public ProgramDto updateProgram(UUID id, ProgramDto dto) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo"));
        
        if (!program.getCode().equals(dto.getCode()) && programRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã chương trình đã tồn tại");
        }

        Major major = majorRepository.findById(dto.getMajorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học"));

        program.setName(dto.getName());
        program.setCode(dto.getCode());
        program.setDescription(dto.getDescription());
        program.setMajor(major);
        program.setStatus(dto.getStatus());
        
        return toDto(programRepository.save(program));
    }

    @Transactional
    public void assignCourses(UUID programId, List<UUID> courseIds) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo"));

        java.util.Set<UUID> uniqueCourseIds = new java.util.HashSet<>(courseIds);

        // Record REMOVED history for those not in the new list
        List<ProgramCourse> currentMappings = programCourseRepository.findAllByProgramId(programId);
        for (ProgramCourse pc : currentMappings) {
            if (!uniqueCourseIds.contains(pc.getCourse().getId())) {
                saveProgramCourseHistory(pc, "REMOVED");
            }
        }

        // Mark all current mappings as deleted first
        programCourseRepository.softDeleteByProgramId(programId);
        entityManager.flush();
        entityManager.clear(); // Clear context to avoid stale entities

        int index = 0;
        for (UUID courseId : uniqueCourseIds) {
            Optional<ProgramCourse> existingPc = programCourseRepository.findByProgramIdAndCourseIdIncludingDeleted(programId, courseId);
            
            if (existingPc.isPresent()) {
                ProgramCourse pc = existingPc.get();
                boolean wasDeleted = pc.isDeleted();
                pc.setDeleted(false);
                pc.setSemester((index / 5) + 1);
                pc = programCourseRepository.save(pc);
                
                saveProgramCourseHistory(pc, wasDeleted ? "RESTORED" : "UPDATED");
            } else {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học: " + courseId));
                
                ProgramCourse pc = ProgramCourse.builder()
                        .program(program)
                        .course(course)
                        .semester((index / 5) + 1)
                        .required(true)
                        .build();
                pc = programCourseRepository.save(pc);
                saveProgramCourseHistory(pc, "ADDED");
            }
            index++;
        }
    }

    private void saveProgramCourseHistory(ProgramCourse pc, String action) {
        com.euni.backend.entity.history.ProgramCourseHistory history = com.euni.backend.entity.history.ProgramCourseHistory.builder()
                .programCourseId(pc.getId())
                .programId(pc.getProgram().getId())
                .courseId(pc.getCourse().getId())
                .semester(pc.getSemester())
                .required(pc.getRequired())
                .action(action)
                .changeReason("Batch Assignment Update")
                .build();
        programCourseHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<UUID> getCourseIdsByProgram(UUID programId) {
        return programCourseRepository.findAllByProgramId(programId).stream()
                .map(pc -> pc.getCourse().getId())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProgram(UUID id) {
        if (!programRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy chương trình đào tạo");
        }
        programRepository.deleteById(id);
    }

    private ProgramDto toDto(Program program) {
        return ProgramDto.builder()
                .id(program.getId())
                .name(program.getName())
                .code(program.getCode())
                .description(program.getDescription())
                .majorId(program.getMajor().getId())
                .majorName(program.getMajor().getName())
                .status(program.getStatus())
                .generalObjective(program.getGeneralObjective())
                .specificObjectives(program.getSpecificObjectives())
                .learningOutcomes(program.getLearningOutcomes())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .build();
    }
}
