package com.euni.backend.dto.request;

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
public class SurveySubmissionRequest {
    private String respondentName;
    private String respondentEmail;
    private String respondentPhone;
    private String organization;
    private String position;

    @Builder.Default
    private List<AnswerItem> answers = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItem {
        private Long questionId;
        private String textValue;
        private Integer numericValue;
        private List<String> selectedOptions;
    }
}
