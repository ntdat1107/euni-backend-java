package com.euni.backend.service;

import com.euni.backend.dto.ProgramObeMatrixResponse;
import com.euni.backend.dto.UpdateCoursePiMapRequest;
import com.euni.backend.entity.*;
import com.euni.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramObeService {

    private final ProgramRepository programRepository;
    private final ProgramObjectiveRepository programObjectiveRepository;
    private final ProgramLearningOutcomeRepository programLearningOutcomeRepository;
    private final PerformanceIndicatorRepository performanceIndicatorRepository;
    private final ObjectivePiMapRepository objectivePiMapRepository;
    private final CoursePiMapRepository coursePiMapRepository;
    private final ProgramCourseRepository programCourseRepository;

    @Transactional(readOnly = true)
    public ProgramObeMatrixResponse getProgramObe(Long programId) {
        Program program = programRepository.findActiveById(programId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo: " + programId));

        List<ProgramObjective> objectives = programObjectiveRepository.findAllByProgramId(programId);
        List<ProgramLearningOutcome> outcomes = programLearningOutcomeRepository.findAllByProgramId(programId);
        List<ObjectivePiMap> objPiMaps = objectivePiMapRepository.findAllByProgramId(programId);
        List<CoursePiMap> coursePiMaps = coursePiMapRepository.findAllByProgramId(programId);
        List<ProgramCourse> programCourses = programCourseRepository.findAllByProgramId(programId);

        // Map POs
        List<ProgramObeMatrixResponse.ObjectiveDto> objDtos = objectives.stream()
                .map(po -> ProgramObeMatrixResponse.ObjectiveDto.builder()
                        .id(po.getId())
                        .code(po.getCode())
                        .content(po.getContent())
                        .orderIndex(po.getOrderIndex())
                        .build())
                .collect(Collectors.toList());

        // Map PLOs with PIs
        List<ProgramObeMatrixResponse.LearningOutcomeDto> outcomeDtos = outcomes.stream()
                .map(plo -> {
                    List<ProgramObeMatrixResponse.IndicatorDto> piDtos = plo.getIndicators().stream()
                            .filter(pi -> !pi.isDeleted())
                            .sorted(Comparator.comparingInt(pi -> pi.getOrderIndex() != null ? pi.getOrderIndex() : 0))
                            .map(pi -> ProgramObeMatrixResponse.IndicatorDto.builder()
                                    .id(pi.getId())
                                    .outcomeId(plo.getId())
                                    .outcomeCode(plo.getCode())
                                    .code(pi.getCode())
                                    .content(pi.getContent())
                                    .bloomLevel(pi.getBloomLevel())
                                    .orderIndex(pi.getOrderIndex())
                                    .build())
                            .collect(Collectors.toList());

                    return ProgramObeMatrixResponse.LearningOutcomeDto.builder()
                            .id(plo.getId())
                            .code(plo.getCode())
                            .name(plo.getName())
                            .content(plo.getContent())
                            .domain(plo.getDomain())
                            .orderIndex(plo.getOrderIndex())
                            .indicators(piDtos)
                            .build();
                })
                .collect(Collectors.toList());

        // Map Objective-PI Maps
        List<ProgramObeMatrixResponse.ObjectivePiMapDto> objPiMapDtos = objPiMaps.stream()
                .map(m -> ProgramObeMatrixResponse.ObjectivePiMapDto.builder()
                        .id(m.getId())
                        .objectiveId(m.getObjective().getId())
                        .objectiveCode(m.getObjective().getCode())
                        .piId(m.getPerformanceIndicator().getId())
                        .piCode(m.getPerformanceIndicator().getCode())
                        .note(m.getNote())
                        .build())
                .collect(Collectors.toList());

        // Group CoursePiMap by ProgramCourseId
        Map<Long, Map<Long, Integer>> pcPiLevelMap = new HashMap<>();
        Set<Long> coveredPiIds = new HashSet<>();
        for (CoursePiMap cpm : coursePiMaps) {
            if (cpm.getLevel() != null && cpm.getLevel() > 0) {
                pcPiLevelMap.computeIfAbsent(cpm.getProgramCourse().getId(), k -> new HashMap<>())
                        .put(cpm.getPerformanceIndicator().getId(), cpm.getLevel());
                coveredPiIds.add(cpm.getPerformanceIndicator().getId());
            }
        }

        // Build Course Matrix Rows
        List<ProgramObeMatrixResponse.CoursePiMatrixRowDto> rowDtos = programCourses.stream()
                .map(pc -> {
                    Map<Long, Integer> piLevels = pcPiLevelMap.getOrDefault(pc.getId(), Collections.emptyMap());
                    List<ProgramObeMatrixResponse.CoursePiCellDto> cells = piLevels.entrySet().stream()
                            .map(e -> ProgramObeMatrixResponse.CoursePiCellDto.builder()
                                    .piId(e.getKey())
                                    .level(e.getValue())
                                    .build())
                            .collect(Collectors.toList());

                    return ProgramObeMatrixResponse.CoursePiMatrixRowDto.builder()
                            .programCourseId(pc.getId())
                            .courseId(pc.getCourse().getId())
                            .courseCode(pc.getCourse().getCode())
                            .courseName(pc.getCourse().getName())
                            .credits(pc.getCourse().getCredits())
                            .semester(pc.getSemester())
                            .structureId(pc.getStructure() != null ? pc.getStructure().getId() : null)
                            .structureName(pc.getStructure() != null ? pc.getStructure().getName() : null)
                            .cells(cells)
                            .build();
                })
                .collect(Collectors.toList());

        // Identify uncovered PIs (not covered by any course)
        List<String> uncoveredPiCodes = new ArrayList<>();
        for (ProgramLearningOutcome plo : outcomes) {
            for (PerformanceIndicator pi : plo.getIndicators()) {
                if (!pi.isDeleted() && !coveredPiIds.contains(pi.getId())) {
                    uncoveredPiCodes.add(pi.getCode());
                }
            }
        }

        return ProgramObeMatrixResponse.builder()
                .programId(program.getId())
                .programCode(program.getCode())
                .programName(program.getName())
                .objectives(objDtos)
                .learningOutcomes(outcomeDtos)
                .objectivePiMaps(objPiMapDtos)
                .courseMatrixRows(rowDtos)
                .uncoveredPiCodes(uncoveredPiCodes)
                .build();
    }

    @Transactional
    public void updateCoursePiMap(UpdateCoursePiMapRequest request) {
        ProgramCourse pc = programCourseRepository.findById(request.getProgramCourseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học trong CTĐT: " + request.getProgramCourseId()));

        if (request.getMappings() == null) return;

        for (UpdateCoursePiMapRequest.PiLevelItem item : request.getMappings()) {
            PerformanceIndicator pi = performanceIndicatorRepository.findActiveById(item.getPiId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy PI: " + item.getPiId()));

            Optional<CoursePiMap> existing = coursePiMapRepository.findByProgramCourseIdAndPiId(pc.getId(), pi.getId());

            if (item.getLevel() == null || item.getLevel() <= 0) {
                existing.ifPresent(coursePiMapRepository::delete);
            } else {
                CoursePiMap map = existing.orElseGet(() -> CoursePiMap.builder()
                        .programCourse(pc)
                        .performanceIndicator(pi)
                        .build());
                map.setLevel(item.getLevel());
                coursePiMapRepository.save(map);
            }
        }
    }

    @Transactional
    public ProgramObjective createObjective(Long programId, ProgramObjective po) {
        Program program = programRepository.findActiveById(programId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo: " + programId));
        po.setProgram(program);
        return programObjectiveRepository.save(po);
    }

    @Transactional
    public ProgramLearningOutcome createLearningOutcome(Long programId, ProgramLearningOutcome plo) {
        Program program = programRepository.findActiveById(programId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo: " + programId));
        plo.setProgram(program);
        return programLearningOutcomeRepository.save(plo);
    }

    @Transactional
    public PerformanceIndicator createIndicator(Long outcomeId, PerformanceIndicator pi) {
        ProgramLearningOutcome outcome = programLearningOutcomeRepository.findActiveById(outcomeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy PLO: " + outcomeId));
        pi.setOutcome(outcome);
        return performanceIndicatorRepository.save(pi);
    }
}
