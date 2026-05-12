package com.euni.backend.service;

import com.euni.backend.dto.RoleDto;
import com.euni.backend.entity.Permission;
import com.euni.backend.entity.Role;
import com.euni.backend.mapper.RoleMapper;
import com.euni.backend.repository.PermissionRepository;
import com.euni.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAllActive().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleDto createRole(RoleDto roleDto) {
        Role role = roleMapper.toEntity(roleDto);
        return roleMapper.toDto(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        role.setDeleted(true);
        roleRepository.save(role);
    }

    @Transactional
    public RoleDto updateRolePermissions(UUID roleId, List<UUID> permissionIds) {
        Role role = roleRepository.findActiveById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.setPermissions(new HashSet<>(permissions));
        
        return roleMapper.toDto(roleRepository.save(role));
    }
}
