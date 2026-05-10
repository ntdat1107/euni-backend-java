package com.euni.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private UUID id;
    private String name;
    private String code;
    private Integer credits;
    private String description;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
