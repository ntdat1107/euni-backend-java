package com.euni.backend.seeder;

import com.euni.backend.entity.Faculty;
import com.euni.backend.entity.Permission;
import com.euni.backend.entity.Role;
import com.euni.backend.entity.User;
import com.euni.backend.repository.FacultyRepository;
import com.euni.backend.repository.PermissionRepository;
import com.euni.backend.repository.RoleRepository;
import com.euni.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.seeder.database.enabled", havingValue = "true")
public class DatabaseSeeder implements CommandLineRunner {

    private final FacultyRepository facultyRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database seeding...");

        // 1. Seed Faculties
        Faculty itDept = seedFaculty(UUID.fromString("d1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c6d"), "Khoa Công nghệ Thông tin", "IT_FACULTY");
        Faculty seFaculty = seedFaculty(UUID.fromString("d2c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c6e"), "Khoa Công nghệ Phần mềm", "SE_FACULTY");

        // 2. Seed Permissions
        Permission makerPerm = seedPermission(UUID.fromString("f1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c61"), "Người lập biểu", "MAKER", "Có quyền tạo và chỉnh sửa hồ sơ");
        Permission approverPerm = seedPermission(UUID.fromString("f1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c62"), "Người phê duyệt", "APPROVER", "Có quyền kiểm tra và phê duyệt hồ sơ");
        Permission viewerPerm = seedPermission(UUID.fromString("f1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c63"), "Người xem", "VIEWER", "Chỉ có quyền xem thông tin");

        // 3. Seed Roles
        Role adminRole = seedRole(UUID.fromString("a1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c6f"), "Quản trị viên", "ADMIN",
            new HashSet<>(Set.of(makerPerm, approverPerm, viewerPerm)));

        Role lecturerRole = seedRole(UUID.fromString("a1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c60"), "Giảng viên", "LECTURER",
            new HashSet<>(Set.of(makerPerm, viewerPerm)));

        // 4. Seed Users
        seedUser(UUID.fromString("b1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c61"), "admin", "admin@euni.edu.vn", "Quản trị viên Hệ thống", "admin123", "ADM-001", "Active", itDept, new HashSet<>(Set.of(adminRole)), 1L);
        seedUser(UUID.fromString("b1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c62"), "lecturer_a", "lecturer_a@euni.edu.vn", "Nguyễn Văn A", "password", "GV-001", "Active", seFaculty, new HashSet<>(Set.of(lecturerRole)), 0L);

        log.info("Database seeding completed.");
    }

    private Faculty seedFaculty(UUID id, String name, String code) {
        return facultyRepository.findById(id).orElseGet(() -> {
            Faculty faculty = new Faculty();
            faculty.setId(id);
            faculty.setName(name);
            faculty.setCode(code);
            return facultyRepository.save(faculty);
        });
    }

    private Permission seedPermission(UUID id, String name, String code, String description) {
        return permissionRepository.findById(id).orElseGet(() -> {
            Permission perm = new Permission();
            perm.setId(id);
            perm.setName(name);
            perm.setCode(code);
            perm.setDescription(description);
            return permissionRepository.save(perm);
        });
    }

    private Role seedRole(UUID id, String name, String code, Set<Permission> permissions) {
        return roleRepository.findById(id).orElseGet(() -> {
            Role role = new Role();
            role.setId(id);
            role.setName(name);
            role.setCode(code);
            role.setPermissions(permissions);
            return roleRepository.save(role);
        });
    }

    private void seedUser(UUID id, String username, String email, String fullName, String rawPassword, String employeeId, String status, Faculty faculty, Set<Role> roles, Long tokenVersion) {
        User user = userRepository.findByUsername(username).orElseGet(User::new);
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmployeeId(employeeId);
        user.setStatus(status);
        user.setFaculty(faculty);
        user.setRoles(roles);
        user.setTokenVersion(tokenVersion);
        userRepository.save(user);
    }
}
