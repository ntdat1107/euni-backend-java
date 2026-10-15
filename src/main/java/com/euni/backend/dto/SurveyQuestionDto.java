package com.euni.backend.dto;

import com.euni.backend.entity.enums.SurveyQuestionType;
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
public class SurveyQuestionDto {
    private Long id;
    private Long sectionId;
    private String content;
    private String helpText;
    private SurveyQuestionType questionType;
    private boolean required;
    private Integer orderIndex;
    private Integer minRating;
    private Integer maxRating;
    @Builder.Default
    private List<SurveyOptionDto> options = new ArrayList<>();
}
