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
public class CourseDto {
    private Long id;
    private String name;
    private String code;
    private Integer credits;
    private String description;
    private String data;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
