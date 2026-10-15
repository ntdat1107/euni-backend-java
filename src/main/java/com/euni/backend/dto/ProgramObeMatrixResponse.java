package com.euni.backend.dto;

import com.euni.backend.entity.enums.BloomLevel;
import com.euni.backend.entity.enums.DomainType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramObeMatrixResponse {
    private Long programId;
    private String programCode;
    private String programName;

    @Builder.Default
    private List<ObjectiveDto> objectives = new ArrayList<>();

    @Builder.Default
    private List<LearningOutcomeDto> learningOutcomes = new ArrayList<>();

    @Builder.Default
    private List<ObjectivePiMapDto> objectivePiMaps = new ArrayList<>();

    @Builder.Default
    private List<CoursePiMatrixRowDto> courseMatrixRows = new ArrayList<>();

    @Builder.Default
    private List<String> uncoveredPiCodes = new ArrayList<>(); // PI chưa có môn nào đảm nhiệm

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectiveDto {
        private Long id;
        private String code;
        private String content;
        private Integer orderIndex;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningOutcomeDto {
        private Long id;
        private String code;
        private String name;
        private String content;
        private DomainType domain;
        private Integer orderIndex;
        @Builder.Default
        private List<IndicatorDto> indicators = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicatorDto {
        private Long id;
        private Long outcomeId;
        private String outcomeCode;
        private String code;
        private String content;
        private BloomLevel bloomLevel;
        private Integer orderIndex;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectivePiMapDto {
        private Long id;
        private Long objectiveId;
        private String objectiveCode;
        private Long piId;
        private String piCode;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoursePiMatrixRowDto {
        private Long programCourseId;
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Integer credits;
        private Integer semester;
        private Long structureId;
        private String structureName;
        @Builder.Default
        private List<CoursePiCellDto> cells = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoursePiCellDto {
        private Long piId;
        private String piCode;
        private Integer level; // 1, 2, 3
    }
}
