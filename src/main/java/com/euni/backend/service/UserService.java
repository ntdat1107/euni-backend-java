package com.euni.backend.service;

import com.euni.backend.dto.UserDto;
import com.euni.backend.entity.User;
import com.euni.backend.entity.Faculty;
import com.euni.backend.repository.FacultyRepository;
import com.euni.backend.entity.Role;
import com.euni.backend.mapper.UserMapper;
import com.euni.backend.repository.UserRepository;
import com.euni.backend.repository.RoleRepository;
import com.euni.backend.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FacultyRepository facultyRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAllActive().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toDto(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserStatus(UUID id, String status) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode("EUni@2026")); // Default password
        user.setTokenVersion(0L); // Force password change on first login
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.PENDING.getValue());
        }
        
        // Map Faculty
        if (userDto.getFaculty() != null) {
            facultyRepository.findByCode(userDto.getFaculty())
                    .ifPresent(user::setFaculty);
        }
        
        // Map Roles
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            java.util.Set<Role> roles = userDto.getRoles().stream()
                    .map(roleRepository::findByCode)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(java.util.stream.Collectors.toSet());
            user.setRoles(roles);
        }
        
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUser(UUID id, UserDto userDto) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setEmployeeId(userDto.getEmployeeId());
        
        // Update Faculty
        if (userDto.getFaculty() != null) {
            facultyRepository.findByCode(userDto.getFaculty())
                    .ifPresent(user::setFaculty);
        }
        
        // Update Roles
        if (userDto.getRoles() != null) {
            java.util.Set<Role> roles = userDto.getRoles().stream()
                    .map(roleRepository::findByCode)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(java.util.stream.Collectors.toSet());
            user.setRoles(roles);
        }

        // Update Status
        if (userDto.getStatus() != null) {
            user.setStatus(userDto.getStatus());
        }
        
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    public java.util.Map<String, String> resetPassword(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String tempPassword = "Temp" + java.util.UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setTokenVersion(user.getTokenVersion() + 1); // Invalidate old tokens
        
        userRepository.save(user);
        return java.util.Map.of("tempPassword", tempPassword);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password does not match");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1); // Invalidate old tokens
        
        userRepository.save(user);
    }
}
