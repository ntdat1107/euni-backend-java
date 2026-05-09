package com.euni.backend.mapper;

import com.euni.backend.dto.PermissionDto;
import com.euni.backend.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDto toDto(Permission permission);
    Permission toEntity(PermissionDto dto);
}
