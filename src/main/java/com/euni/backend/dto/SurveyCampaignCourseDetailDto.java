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
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String status;
    private String syllabusData;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
