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
    private String name;
    private String code;
    private String description;
    private Long majorId;
    private String majorName;
    private ProgramStatus status;
    private String generalObjective;
    private String specificObjectives;
    private String learningOutcomes;
    private String data;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean hasUncompletedCampaign;
}
