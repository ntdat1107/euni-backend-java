package com.euni.backend.dto;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignCourseMetaDto {
    private UUID id; // survey_campaign_course id
    private UUID courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String status;
}
