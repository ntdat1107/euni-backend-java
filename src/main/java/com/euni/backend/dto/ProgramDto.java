package com.euni.backend.dto;

import com.euni.backend.entity.enums.ProgramStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramDto {
    private Long id;
    private Long parentId;
    private String parentCode;
    private String name;
    private String code;
    private String description;
    private Long majorId;
    private String majorName;
    private ProgramStatus status;
    private Integer effectiveYear;
    private Integer totalCredits;
    private String degreeLevel;
    private String trainingMode;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean hasUncompletedCampaign;
}
