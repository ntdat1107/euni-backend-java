package com.euni.backend.dto.request;

import com.euni.backend.dto.SurveyQuestionDto;
import com.euni.backend.dto.SurveySectionDto;
import jakarta.validation.constraints.NotBlank;
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
public class SurveyFormSaveRequest {
    @NotBlank(message = "Tiêu đề khảo sát không được để trống")
    private String title;
    private String description;
    private String targetAudience;
    private boolean anonymous;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    @Builder.Default
    private List<SurveySectionDto> sections = new ArrayList<>();
    @Builder.Default
    private List<SurveyQuestionDto> directQuestions = new ArrayList<>();
}
