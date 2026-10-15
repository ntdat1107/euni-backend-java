package com.euni.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCoursePiMapRequest {
    @NotNull(message = "programCourseId không được để trống")
    private Long programCourseId;
    
    private List<PiLevelItem> mappings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PiLevelItem {
        @NotNull(message = "piId không được để trống")
        private Long piId;
        
        // level: 1 (I), 2 (T), 3 (U), hoặc null/0 nếu bỏ chọn
        private Integer level;
    }
}
