package com.euni.backend.service;

import com.euni.backend.dto.ProgramObeMatrixResponse;
import com.euni.backend.dto.UpdateCoursePiMapRequest;
import com.euni.backend.entity.*;
import com.euni.backend.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    private final SyllabusRepository syllabusRepository;
    private final CloPiMapRepository cloPiMapRepository;
    private final CourseLearningOutcomeRepository courseLearningOutcomeRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ProgramObeMatrixResponse getProgramObe(Long programId) {
        Program program = programRepository.findActiveById(programId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình đào tạo: " + programId));

        List<ProgramObjective> objectives = programObjectiveRepository.findAllByProgramId(programId);
        List<ProgramLearningOutcome> outcomes = programLearningOutcomeRepository.findAllByProgramId(programId);
        List<ObjectivePiMap> objPiMaps = objectivePiMapRepository.findAllByProgramId(programId);
        List<CoursePiMap> coursePiMaps = coursePiMapRepository.findAllByProgramId(programId);
        List<ProgramCourse> programCourses = programCourseRepository.findAllByProgramId(programId);

        List<ProgramObeMatrixResponse.ObjectiveDto> objDtos = objectives.stream()
                .map(po -> ProgramObeMatrixResponse.ObjectiveDto.builder()
                        .id(po.getId())
                        .code(po.getCode())
                        .content(po.getContent())
                        .orderIndex(po.getOrderIndex())
                        .build())
                .collect(Collectors.toList());

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

        Map<Long, Map<Long, Integer>> pcPiLevelMap = new HashMap<>();
        Set<Long> coveredPiIds = new HashSet<>();
        for (CoursePiMap cpm : coursePiMaps) {
            if (cpm.getLevel() != null && cpm.getLevel() > 0) {
                pcPiLevelMap.computeIfAbsent(cpm.getProgramCourse().getId(), k -> new HashMap<>())
                        .put(cpm.getPerformanceIndicator().getId(), cpm.getLevel());
                coveredPiIds.add(cpm.getPerformanceIndicator().getId());
            }
        }

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
    public void batchUpdateCoursePiMap(Long programId, List<UpdateCoursePiMapRequest> requests) {
        if (requests == null || requests.isEmpty()) return;
        for (UpdateCoursePiMapRequest req : requests) {
            updateCoursePiMap(req);
        }
    }

    @Transactional
    public ProgramObeMatrixResponse autoDeriveMatrixFromSyllabuses(Long programId) {
        List<ProgramCourse> pcs = programCourseRepository.findAllByProgramId(programId);
        List<PerformanceIndicator> allPis = performanceIndicatorRepository.findAllByProgramId(programId);
        Map<String, PerformanceIndicator> piByCode = allPis.stream()
                .collect(Collectors.toMap(PerformanceIndicator::getCode, p -> p, (a, b) -> a));

        for (ProgramCourse pc : pcs) {
            Optional<Syllabus> sylOpt = syllabusRepository.findFirstByProgramCourseIdAndDeletedFalseOrderByVersionDesc(pc.getId());
            if (sylOpt.isEmpty()) continue;
            Syllabus syl = sylOpt.get();

            List<CloPiMap> cloPiMaps = cloPiMapRepository.findAllBySyllabusId(syl.getId());
            Map<Long, Integer> maxLevelByPiId = new HashMap<>();

            for (CloPiMap cpm : cloPiMaps) {
                Long piId = cpm.getPerformanceIndicator().getId();
                String bloom = cpm.getClo().getBloomLevel();
                int level = mapBloomToLevel(bloom, pc.getSemester(), pc.getCourse().getName());
                maxLevelByPiId.merge(piId, level, Math::max);
            }

            if (maxLevelByPiId.isEmpty() && syl.getWeeklySchedule() != null && !syl.getWeeklySchedule().isBlank()) {
                extractLevelsFromJson(syl.getWeeklySchedule(), piByCode, pc.getSemester(), pc.getCourse().getName(), maxLevelByPiId);
            }

            for (Map.Entry<Long, Integer> entry : maxLevelByPiId.entrySet()) {
                Long piId = entry.getKey();
                Integer level = entry.getValue();
                PerformanceIndicator pi = performanceIndicatorRepository.findById(piId).orElse(null);
                if (pi == null) continue;

                Optional<CoursePiMap> existing = coursePiMapRepository.findByProgramCourseIdAndPiId(pc.getId(), piId);
                CoursePiMap map = existing.orElseGet(() -> CoursePiMap.builder()
                        .programCourse(pc)
                        .performanceIndicator(pi)
                        .build());
                map.setLevel(level);
                coursePiMapRepository.save(map);
            }
        }

        return getProgramObe(programId);
    }

    private void extractLevelsFromJson(String json, Map<String, PerformanceIndicator> piByCode, Integer semester, String courseName, Map<Long, Integer> maxLevelByPiId) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.has("clos") || !root.get("clos").isArray()) return;

            for (JsonNode cloNode : root.get("clos")) {
                String bloom = cloNode.has("bloomLevel") ? cloNode.get("bloomLevel").asText("APPLY") : "APPLY";
                int level = mapBloomToLevel(bloom, semester, courseName);

                if (cloNode.has("mappedPlos") && cloNode.get("mappedPlos").isArray()) {
                    for (JsonNode mappedPiNode : cloNode.get("mappedPlos")) {
                        String piCode = mappedPiNode.asText("");
                        PerformanceIndicator pi = piByCode.get(piCode);
                        if (pi != null) {
                            maxLevelByPiId.merge(pi.getId(), level, Math::max);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing syllabus json for auto-derive", e);
        }
    }

    private int mapBloomToLevel(String bloom, Integer semester, String courseName) {
        if (bloom == null) bloom = "APPLY";
        String normalized = bloom.toUpperCase();

        if (semester != null && semester >= 7) {
            String nameLower = courseName != null ? courseName.toLowerCase() : "";
            if (nameLower.contains("tốt nghiệp") || nameLower.contains("khóa luận") || nameLower.contains("đồ án")) {
                return 4; // A - Assess
            }
        }

        if (normalized.contains("1") || normalized.contains("REMEMBER") || normalized.contains("2") || normalized.contains("UNDERSTAND")) {
            return 1; // I - Introduce
        } else if (normalized.contains("3") || normalized.contains("APPLY") || normalized.contains("4") || normalized.contains("ANALYZE")) {
            return 2; // R - Reinforce
        } else if (normalized.contains("5") || normalized.contains("EVALUATE") || normalized.contains("6") || normalized.contains("CREATE")) {
            return 3; // M - Master
        }
        return 2;
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
