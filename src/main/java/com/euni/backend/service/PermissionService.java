package com.euni.backend.service;

import com.euni.backend.dto.PermissionDto;
import com.euni.backend.entity.Permission;
import com.euni.backend.mapper.PermissionMapper;
import com.euni.backend.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAllActive().stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PermissionDto createPermission(PermissionDto permissionDto) {
        Permission permission = permissionMapper.toEntity(permissionDto);
        return permissionMapper.toDto(permissionRepository.save(permission));
    }

    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        permission.setDeleted(true);
        permissionRepository.save(permission);
    }
}
