package com.euni.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorDto {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long facultyId;
    private String facultyName;
    private int programCount;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
