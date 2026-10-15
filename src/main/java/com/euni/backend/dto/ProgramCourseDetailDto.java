package com.euni.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramCourseDetailDto {
    private Long id;
    private Long programId;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private Integer semester;
    private Boolean required;
    private Long structureId;
    private String structureName;
    private Long electiveGroupId;
    private String electiveGroupName;
}
