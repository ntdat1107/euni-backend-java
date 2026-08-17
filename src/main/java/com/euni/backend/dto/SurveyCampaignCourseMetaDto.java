package com.euni.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignCourseMetaDto {
    private Long id; // survey_campaign_course id
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String status;
}
