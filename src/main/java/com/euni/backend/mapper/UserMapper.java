package com.euni.backend.mapper;

import com.euni.backend.dto.UserDto;
import com.euni.backend.entity.Role;
import com.euni.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "faculty", source = "faculty.code")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserDto toDto(User user);

    @Mapping(target = "faculty", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toEntity(UserDto dto);

    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(Role::getCode).collect(Collectors.toList());
    }
}
