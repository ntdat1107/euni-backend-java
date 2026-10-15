package com.euni.backend.service;

import com.euni.backend.dto.ProgramCourseDetailDto;
import com.euni.backend.dto.ProgramDto;
import com.euni.backend.entity.Course;
import com.euni.backend.entity.Major;
import com.euni.backend.entity.Program;
import com.euni.backend.entity.ProgramCourse;
import com.euni.backend.entity.ProgramStructure;
import com.euni.backend.entity.ProgramElectiveGroup;
import com.euni.backend.entity.enums.ProgramStatus;
import com.euni.backend.entity.enums.SurveyCampaignStatus;
import com.euni.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final MajorRepository majorRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final CourseRepository courseRepository;
    private final ProgramCourseHistoryRepository programCourseHistoryRepository;
    private final SurveyCampaignRepository surveyCampaignRepository;
    private final ProgramStructureRepository programStructureRepository;
    private final ProgramElectiveGroupRepository programElectiveGroupRepository;
    private final jakarta.persistence.EntityManager entityManager;

    private void checkActiveSurveyCampaign(Long programId) {
        if (surveyCampaignRepository.existsByProgramIdAndStatusNotIn(
                programId, 
                List.of(SurveyCampaignStatus.COMPLETED, SurveyCampaignStatus.APPROVED, SurveyCampaignStatus.CANCELLED))) {
            throw new RuntimeException("Không thể xóa Chương trình đào tạo do đang có Đợt xây dựng/khảo sát chưa hoàn thành!");
        }
    }

    @Transactional(readOnly = true)
    public List<ProgramDto> getAllPrograms() {
        return programRepository.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProgramDto getProgramById(Long id) {
        Program program = programRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo với ID: " + id));
        return toDto(program);
    }

    @Transactional
    public ProgramDto createProgram(ProgramDto dto) {
        if (programRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã chương trình đã tồn tại");
        }
        
        Major major = majorRepository.findActiveById(dto.getMajorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học"));

        Program parent = null;
        if (dto.getParentId() != null) {
            parent = programRepository.findActiveById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình cha: " + dto.getParentId()));
        }

        Program program = Program.builder()
                .parent(parent)
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .major(major)
                .status(dto.getStatus() != null ? dto.getStatus() : ProgramStatus.DRAFT)
                .effectiveYear(dto.getEffectiveYear())
                .totalCredits(dto.getTotalCredits())
                .degreeLevel(dto.getDegreeLevel() != null ? dto.getDegreeLevel() : "ENGINEER")
                .trainingMode(dto.getTrainingMode() != null ? dto.getTrainingMode() : "FULL_TIME")
                .build();
        
        return toDto(programRepository.save(program));
    }

    @Transactional
    public ProgramDto updateProgram(Long id, ProgramDto dto) {
        Program program = programRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo"));
        
        if (!program.getCode().equals(dto.getCode()) && programRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã chương trình đã tồn tại");
        }

        Major major = majorRepository.findActiveById(dto.getMajorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học"));

        if (dto.getParentId() != null) {
            Program parent = programRepository.findActiveById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình cha"));
            program.setParent(parent);
        }

        program.setName(dto.getName());
        program.setCode(dto.getCode());
        program.setDescription(dto.getDescription());
        program.setMajor(major);
        program.setStatus(dto.getStatus());
        if (dto.getEffectiveYear() != null) program.setEffectiveYear(dto.getEffectiveYear());
        if (dto.getTotalCredits() != null) program.setTotalCredits(dto.getTotalCredits());
        if (dto.getDegreeLevel() != null) program.setDegreeLevel(dto.getDegreeLevel());
        if (dto.getTrainingMode() != null) program.setTrainingMode(dto.getTrainingMode());
        
        return toDto(programRepository.save(program));
    }

    @Transactional
    public void assignCourses(Long programId, List<Long> courseIds) {
        Program program = programRepository.findActiveById(programId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo"));

        java.util.Set<Long> uniqueCourseIds = new java.util.HashSet<>(courseIds);

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
        for (Long courseId : uniqueCourseIds) {
            Optional<ProgramCourse> existingPc = programCourseRepository.findByProgramIdAndCourseIdIncludingDeleted(programId, courseId);
            
            if (existingPc.isPresent()) {
                ProgramCourse pc = existingPc.get();
                boolean wasDeleted = pc.isDeleted();
                pc.setDeleted(false);
                pc.setSemester((index / 5) + 1);
                pc = programCourseRepository.save(pc);
                
                saveProgramCourseHistory(pc, wasDeleted ? "RESTORED" : "UPDATED");
            } else {
                Course course = courseRepository.findActiveById(courseId)
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

    @Transactional
    public void assignCoursesWithDetails(Long programId, com.euni.backend.dto.CourseAssignmentRequest request) {
        Program program = programRepository.findActiveById(programId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo"));

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            java.util.Set<Long> uniqueCourseIds = request.getItems().stream()
                    .map(com.euni.backend.dto.CourseAssignmentRequest.CourseItem::getCourseId)
                    .collect(Collectors.toSet());

            List<ProgramCourse> currentMappings = programCourseRepository.findAllByProgramId(programId);
            for (ProgramCourse pc : currentMappings) {
                if (!uniqueCourseIds.contains(pc.getCourse().getId())) {
                    saveProgramCourseHistory(pc, "REMOVED");
                }
            }

            programCourseRepository.softDeleteByProgramId(programId);
            entityManager.flush();
            entityManager.clear();

            for (com.euni.backend.dto.CourseAssignmentRequest.CourseItem item : request.getItems()) {
                Optional<ProgramCourse> existingPc = programCourseRepository.findByProgramIdAndCourseIdIncludingDeleted(programId, item.getCourseId());
                ProgramStructure structure = item.getStructureId() != null
                        ? programStructureRepository.findActiveById(item.getStructureId()).orElse(null)
                        : null;
                ProgramElectiveGroup electiveGroup = item.getElectiveGroupId() != null
                        ? programElectiveGroupRepository.findActiveById(item.getElectiveGroupId()).orElse(null)
                        : null;

                if (existingPc.isPresent()) {
                    ProgramCourse pc = existingPc.get();
                    boolean wasDeleted = pc.isDeleted();
                    pc.setDeleted(false);
                    pc.setStructure(structure);
                    pc.setElectiveGroup(electiveGroup);
                    if (item.getSemester() != null) pc.setSemester(item.getSemester());
                    if (item.getRequired() != null) pc.setRequired(item.getRequired());
                    pc = programCourseRepository.save(pc);
                    saveProgramCourseHistory(pc, wasDeleted ? "RESTORED" : "UPDATED");
                } else {
                    Course course = courseRepository.findActiveById(item.getCourseId())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học: " + item.getCourseId()));
                    ProgramCourse pc = ProgramCourse.builder()
                            .program(program)
                            .course(course)
                            .structure(structure)
                            .electiveGroup(electiveGroup)
                            .semester(item.getSemester() != null ? item.getSemester() : 1)
                            .required(item.getRequired() != null ? item.getRequired() : true)
                            .build();
                    pc = programCourseRepository.save(pc);
                    saveProgramCourseHistory(pc, "ADDED");
                }
            }
        } else if (request.getCourseIds() != null) {
            assignCourses(programId, request.getCourseIds());
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
    public List<Long> getCourseIdsByProgram(Long programId) {
        return programCourseRepository.findAllByProgramId(programId).stream()
                .map(pc -> pc.getCourse().getId())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgramCourseDetailDto> getProgramCoursesDetail(Long programId) {
        return programCourseRepository.findAllByProgramId(programId).stream()
                .map(pc -> ProgramCourseDetailDto.builder()
                        .id(pc.getId())
                        .programId(pc.getProgram().getId())
                        .courseId(pc.getCourse().getId())
                        .courseCode(pc.getCourse().getCode())
                        .courseName(pc.getCourse().getName())
                        .credits(pc.getCourse().getCredits())
                        .semester(pc.getSemester())
                        .required(pc.getRequired())
                        .structureId(pc.getStructure() != null ? pc.getStructure().getId() : null)
                        .structureName(pc.getStructure() != null ? pc.getStructure().getName() : null)
                        .electiveGroupId(pc.getElectiveGroup() != null ? pc.getElectiveGroup().getId() : null)
                        .electiveGroupName(pc.getElectiveGroup() != null ? pc.getElectiveGroup().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProgram(Long id) {
        checkActiveSurveyCampaign(id);

        Program program = programRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo"));

        // Only allow deleting DRAFT programs
        if (program.getStatus() != ProgramStatus.DRAFT) {
            throw new RuntimeException("Chỉ được phép xóa chương trình đào tạo ở trạng thái Bản nháp (DRAFT). Chương trình đang hoặc đã xây dựng không thể xóa!");
        }

        program.setDeleted(true);
        programRepository.save(program);
    }

    public ProgramDto toDto(Program program) {
        if (program == null) return null;
        boolean hasUncompleted = surveyCampaignRepository.existsByProgramIdAndStatusNotIn(
                program.getId(), 
                List.of(SurveyCampaignStatus.COMPLETED, SurveyCampaignStatus.APPROVED, SurveyCampaignStatus.CANCELLED));

        return ProgramDto.builder()
                .id(program.getId())
                .parentId(program.getParent() != null ? program.getParent().getId() : null)
                .parentCode(program.getParent() != null ? program.getParent().getCode() : null)
                .name(program.getName())
                .code(program.getCode())
                .description(program.getDescription())
                .majorId(program.getMajor() != null ? program.getMajor().getId() : null)
                .majorName(program.getMajor() != null ? program.getMajor().getName() : null)
                .status(program.getStatus())
                .effectiveYear(program.getEffectiveYear())
                .totalCredits(program.getTotalCredits())
                .degreeLevel(program.getDegreeLevel())
                .trainingMode(program.getTrainingMode())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .hasUncompletedCampaign(hasUncompleted)
                .build();
    }
}
