package com.euni.backend.dto;

import lombok.*;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignCourseDetailDto {
    private Long id;
    private Long campaignId;
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
    private String reviewNote;
    private String syllabusData;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
