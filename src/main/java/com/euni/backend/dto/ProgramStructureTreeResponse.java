package com.euni.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramStructureTreeResponse {
    private Long id;
    private Long programId;
    private Long parentId;
    private String code;
    private String name;
    private Integer creditRequired;
    private Integer orderIndex;
    private Integer calculatedCredits; // Tổng tín chỉ thực tế của các môn thuộc khối này
    
    @Builder.Default
    private List<ProgramStructureTreeResponse> children = new ArrayList<>();
    
    @Builder.Default
    private List<ProgramElectiveGroupDto> electiveGroups = new ArrayList<>();
}
