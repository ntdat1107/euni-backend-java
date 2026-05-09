package com.euni.backend.controller;

import com.euni.backend.dto.PermissionDto;
import com.euni.backend.dto.RoleDto;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.PermissionService;
import com.euni.backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class RbacController extends BaseController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        return ok(roleService.getAllRoles());
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@RequestBody RoleDto roleDto) {
        return ok(roleService.createRole(roleDto));
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDto>> updateRolePermissions(
            @PathVariable UUID id, 
            @RequestBody Map<String, List<UUID>> request) {
        return ok(roleService.updateRolePermissions(id, request.get("permissionIds")));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionDto>>> getAllPermissions() {
        return ok(permissionService.getAllPermissions());
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionDto>> createPermission(@RequestBody PermissionDto permissionDto) {
        return ok(permissionService.createPermission(permissionDto));
    }

    @DeleteMapping("/permissions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePermission(@PathVariable UUID id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
