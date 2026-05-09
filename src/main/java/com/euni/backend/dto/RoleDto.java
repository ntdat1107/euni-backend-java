package com.euni.backend.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDto {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private List<PermissionDto> permissions;
}
