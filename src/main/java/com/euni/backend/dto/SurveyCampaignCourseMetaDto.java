package com.euni.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignCourseMetaDto {
    private Long id; // survey_campaign_course id
    private Long stepId;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String courseNameEn;
    private Integer credits;
    private Integer theoryCredits;
    private Integer practiceCredits;
    private Integer selfStudyHours;
    private Integer semester;
    private String departmentName;
    private String status;
}
