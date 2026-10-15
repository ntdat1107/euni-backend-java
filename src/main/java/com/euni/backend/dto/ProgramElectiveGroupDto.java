package com.euni.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramElectiveGroupDto {
    private Long id;
    private Long structureId;
    private String name;
    private Integer minCredits;
    private Integer minCourses;
}
