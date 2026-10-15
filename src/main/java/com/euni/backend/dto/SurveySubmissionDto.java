package com.euni.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveySubmissionDto {
    private Long id;
    private Long formId;
    private String respondentName;
    private String respondentEmail;
    private String respondentPhone;
    private String organization;
    private String position;
    private ZonedDateTime submittedAt;
    @Builder.Default
    private List<AnswerDto> answers = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDto {
        private Long id;
        private Long questionId;
        private String questionContent;
        private String questionType;
        private String textValue;
        private Integer numericValue;
        private List<String> selectedOptions;
    }
}
