package com.euni.backend.mapper;

import com.euni.backend.dto.RoleDto;
import com.euni.backend.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    RoleDto toDto(Role role);
    Role toEntity(RoleDto dto);
}
