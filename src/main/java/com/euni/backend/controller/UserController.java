package com.euni.backend.controller;

import com.euni.backend.dto.UserDto;
import com.euni.backend.dto.response.ApiResponse;
import com.euni.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController extends BaseController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        return ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getMe(java.security.Principal principal) {
        return ok(userService.getUserByUsername(principal.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.username == #id")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        return ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ok(null);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(@PathVariable UUID id, @RequestParam String status) {
        userService.updateUserStatus(id, status);
        return ok(null);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody UserDto userDto) {
        return ok(userService.createUser(userDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        return ok(userService.updateUser(id, userDto));
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> resetPassword(@PathVariable UUID id) {
        return ok(userService.resetPassword(id));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody java.util.Map<String, String> request,
            java.security.Principal principal) {
        userService.changePassword(principal.getName(), request.get("oldPassword"), request.get("newPassword"));
        return ok(null);
    }
}
