package com.euni.backend.dto;

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
public class SurveySectionDto {
    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    @Builder.Default
    private List<SurveyQuestionDto> questions = new ArrayList<>();
}
