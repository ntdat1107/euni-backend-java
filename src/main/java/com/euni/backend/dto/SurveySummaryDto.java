package com.euni.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveySummaryDto {
    private Long formId;
    private String formTitle;
    private long totalSubmissions;
    private String publicSlug;
    private String publicUrl;
    private String status;

    @Builder.Default
    private List<QuestionStatDto> questionStats = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionStatDto {
        private Long questionId;
        private String content;
        private String questionType;
        private Double averageScore; // Cho thang đo Likert / Rating
        private Map<String, Long> optionDistribution; // Cho Single/Multiple choice hoặc Likert
        private List<String> textSamples; // Mẫu câu trả lời văn bản
    }
}
