package com.euni.backend.service;

import com.euni.backend.dto.CreateStructureRequest;
import com.euni.backend.dto.ProgramElectiveGroupDto;
import com.euni.backend.dto.ProgramStructureTreeResponse;
import com.euni.backend.entity.Program;
import com.euni.backend.entity.ProgramCourse;
import com.euni.backend.entity.ProgramElectiveGroup;
import com.euni.backend.entity.ProgramStructure;
import com.euni.backend.repository.ProgramCourseRepository;
import com.euni.backend.repository.ProgramElectiveGroupRepository;
import com.euni.backend.repository.ProgramRepository;
import com.euni.backend.repository.ProgramStructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramStructureService {

    private final ProgramStructureRepository programStructureRepository;
    private final ProgramElectiveGroupRepository programElectiveGroupRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final ProgramRepository programRepository;

    @Transactional(readOnly = true)
    public List<ProgramStructureTreeResponse> getStructureTree(Long programId) {
        List<ProgramStructure> allStructures = programStructureRepository.findAllByProgramId(programId);
        List<ProgramCourse> programCourses = programCourseRepository.findAllByProgramId(programId);

        // Map calculated credits for each structure from assigned courses
        Map<Long, Integer> structureCreditsMap = new HashMap<>();
        for (ProgramCourse pc : programCourses) {
            if (pc.getStructure() != null && pc.getCourse() != null) {
                Long structId = pc.getStructure().getId();
                int credits = pc.getCourse().getCredits() != null ? pc.getCourse().getCredits() : 0;
                structureCreditsMap.merge(structId, credits, Integer::sum);
            }
        }

        // Build Tree
        Map<Long, ProgramStructureTreeResponse> dtoMap = new HashMap<>();
        for (ProgramStructure ps : allStructures) {
            List<ProgramElectiveGroupDto> electiveGroupDtos = ps.getElectiveGroups().stream()
                    .filter(eg -> !eg.isDeleted())
                    .map(eg -> ProgramElectiveGroupDto.builder()
                            .id(eg.getId())
                            .structureId(ps.getId())
                            .name(eg.getName())
                            .minCredits(eg.getMinCredits())
                            .minCourses(eg.getMinCourses())
                            .build())
                    .collect(Collectors.toList());

            ProgramStructureTreeResponse node = ProgramStructureTreeResponse.builder()
                    .id(ps.getId())
                    .programId(programId)
                    .parentId(ps.getParent() != null ? ps.getParent().getId() : null)
                    .code(ps.getCode())
                    .name(ps.getName())
                    .creditRequired(ps.getCreditRequired())
                    .orderIndex(ps.getOrderIndex())
                    .calculatedCredits(structureCreditsMap.getOrDefault(ps.getId(), 0))
                    .electiveGroups(electiveGroupDtos)
                    .children(new ArrayList<>())
                    .build();
            dtoMap.put(ps.getId(), node);
        }

        List<ProgramStructureTreeResponse> roots = new ArrayList<>();
        for (ProgramStructure ps : allStructures) {
            ProgramStructureTreeResponse current = dtoMap.get(ps.getId());
            if (ps.getParent() != null && dtoMap.containsKey(ps.getParent().getId())) {
                dtoMap.get(ps.getParent().getId()).getChildren().add(current);
            } else {
                roots.add(current);
            }
        }

        return roots;
    }

    @Transactional
    public ProgramStructureTreeResponse createStructure(Long programId, CreateStructureRequest request) {
        Program program = programRepository.findActiveById(programId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo: " + programId));

        ProgramStructure parent = null;
        if (request.getParentId() != null) {
            parent = programStructureRepository.findActiveById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khối kiến thức cha: " + request.getParentId()));
        }

        if (request.getCreditRequired() == null || request.getCreditRequired() <= 0) {
            throw new RuntimeException("Số tín chỉ yêu cầu phải lớn hơn 0");
        }

        ProgramStructure structure = ProgramStructure.builder()
                .program(program)
                .parent(parent)
                .code(request.getCode())
                .name(request.getName())
                .creditRequired(request.getCreditRequired())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .build();

        ProgramStructure saved = programStructureRepository.save(structure);
        return toResponse(saved);
    }

    @Transactional
    public ProgramStructureTreeResponse updateStructure(Long structureId, CreateStructureRequest request) {
        ProgramStructure structure = programStructureRepository.findActiveById(structureId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khối kiến thức: " + structureId));

        if (request.getCreditRequired() == null || request.getCreditRequired() <= 0) {
            throw new RuntimeException("Số tín chỉ yêu cầu phải lớn hơn 0");
        }

        ProgramStructure parent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(structureId)) {
                throw new RuntimeException("Khối kiến thức không thể là cha của chính nó");
            }
            parent = programStructureRepository.findActiveById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khối kiến thức cha: " + request.getParentId()));
        }

        structure.setParent(parent);
        structure.setCode(request.getCode());
        structure.setName(request.getName());
        structure.setCreditRequired(request.getCreditRequired());
        if (request.getOrderIndex() != null) {
            structure.setOrderIndex(request.getOrderIndex());
        }

        return toResponse(programStructureRepository.save(structure));
    }

    @Transactional
    public void reorderStructures(List<Long> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) return;
        for (int i = 0; i < orderedIds.size(); i++) {
            Long structId = orderedIds.get(i);
            Optional<ProgramStructure> opt = programStructureRepository.findActiveById(structId);
            if (opt.isPresent()) {
                ProgramStructure ps = opt.get();
                ps.setOrderIndex(i);
                programStructureRepository.save(ps);
            }
        }
    }

    @Transactional
    public void deleteStructure(Long structureId) {
        ProgramStructure structure = programStructureRepository.findActiveById(structureId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khối kiến thức: " + structureId));
        structure.setDeleted(true);
        programStructureRepository.save(structure);
    }

    @Transactional
    public ProgramElectiveGroupDto addElectiveGroup(Long structureId, ProgramElectiveGroupDto dto) {
        ProgramStructure structure = programStructureRepository.findActiveById(structureId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khối kiến thức: " + structureId));

        ProgramElectiveGroup group = ProgramElectiveGroup.builder()
                .structure(structure)
                .name(dto.getName())
                .minCredits(dto.getMinCredits() != null ? dto.getMinCredits() : 0)
                .minCourses(dto.getMinCourses() != null ? dto.getMinCourses() : 0)
                .build();

        ProgramElectiveGroup saved = programElectiveGroupRepository.save(group);
        return ProgramElectiveGroupDto.builder()
                .id(saved.getId())
                .structureId(structureId)
                .name(saved.getName())
                .minCredits(saved.getMinCredits())
                .minCourses(saved.getMinCourses())
                .build();
    }

    private ProgramStructureTreeResponse toResponse(ProgramStructure ps) {
        return ProgramStructureTreeResponse.builder()
                .id(ps.getId())
                .programId(ps.getProgram().getId())
                .parentId(ps.getParent() != null ? ps.getParent().getId() : null)
                .code(ps.getCode())
                .name(ps.getName())
                .creditRequired(ps.getCreditRequired())
                .orderIndex(ps.getOrderIndex())
                .calculatedCredits(0)
                .build();
    }
}
