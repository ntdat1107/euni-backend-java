package com.euni.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseAssignmentRequest {
    private List<Long> courseIds;
    private List<CourseItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseItem {
        private Long courseId;
        private Long structureId;
        private Long electiveGroupId;
        private Integer semester;
        private Boolean required;
    }
}
