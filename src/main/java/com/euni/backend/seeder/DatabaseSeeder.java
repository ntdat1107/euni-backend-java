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
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.seeder.database.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final FacultyRepository facultyRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting safe database seeding (natural key existence checks)...");

        // 1. Seed Faculties
        Faculty fitFaculty = seedFaculty("Khoa Công nghệ Thông tin", "IT_FACULTY");
        Faculty seFaculty = seedFaculty("Khoa Kỹ thuật Phần mềm", "SE_FACULTY");
        Faculty csFaculty = seedFaculty("Khoa Khoa học Máy tính", "CS_FACULTY");
        Faculty isFaculty = seedFaculty("Khoa Hệ thống Thông tin", "IS_FACULTY");

        // 2. Seed Permissions
        Permission makerPerm = seedPermission("Người lập biểu", "MAKER", "Có quyền tạo và chỉnh sửa hồ sơ khảo sát");
        Permission approverPerm = seedPermission("Người phê duyệt", "APPROVER", "Có quyền kiểm tra và phê duyệt hồ sơ khảo sát");
        Permission viewerPerm = seedPermission("Người xem", "VIEWER", "Chỉ có quyền xem thông tin");
        Permission adminPerm = seedPermission("Toàn quyền hệ thống", "ADMIN_ALL", "Có tất cả các quyền quản trị");

        // 3. Seed Roles (8 Roles chuẩn)
        Role adminRole = seedRole("Quản trị hệ thống", "ADMIN", new HashSet<>(Set.of(adminPerm, makerPerm, approverPerm, viewerPerm)));
        Role dtdhRole = seedRole("Chuyên viên ĐTĐH", "DTDH", new HashSet<>(Set.of(makerPerm, approverPerm, viewerPerm)));
        Role deanRole = seedRole("Trưởng Khoa", "DEAN", new HashSet<>(Set.of(approverPerm, makerPerm, viewerPerm)));
        Role headDeptRole = seedRole("Trưởng Bộ môn", "HEAD_DEPT", new HashSet<>(Set.of(approverPerm, makerPerm, viewerPerm)));
        Role lecturerRole = seedRole("Giảng viên", "LECTURER", new HashSet<>(Set.of(makerPerm, viewerPerm)));
        Role committeeRole = seedRole("Tổ soạn thảo", "COMMITTEE", new HashSet<>(Set.of(makerPerm, viewerPerm)));
        Role councilRole = seedRole("Hội đồng thẩm định", "COUNCIL", new HashSet<>(Set.of(approverPerm, viewerPerm)));
        Role qaRole = seedRole("Chuyên viên ĐBCL", "QA", new HashSet<>(Set.of(makerPerm, viewerPerm)));

        // 4. Seed Users
        seedUser("admin", "admin@euni.edu.vn", "Quản trị viên Hệ thống", "admin123", "ADM-001", "Active", fitFaculty, new HashSet<>(Set.of(adminRole)), 1L);
        seedUser("cv_dtdh", "dtdh@euni.edu.vn", "Chuyên viên Phòng ĐTĐH", "password", "ĐT-001", "Active", fitFaculty, new HashSet<>(Set.of(dtdhRole)), 0L);
        seedUser("truongkhoa_cntt", "truongkhoa@euni.edu.vn", "PGS.TS. Trần Văn Trưởng", "password", "TK-001", "Active", fitFaculty, new HashSet<>(Set.of(deanRole)), 0L);
        seedUser("truongbomon_se", "truongbomon.se@euni.edu.vn", "TS. Nguyễn Văn Bộ", "password", "BM-001", "Active", seFaculty, new HashSet<>(Set.of(headDeptRole)), 0L);
        seedUser("lecturer_nguyen", "nguyen.va@euni.edu.vn", "ThS. Nguyễn Văn A", "password", "GV-001", "Active", seFaculty, new HashSet<>(Set.of(lecturerRole)), 0L);
        seedUser("lecturer_tran", "tran.tb@euni.edu.vn", "TS. Trần Thị B", "password", "GV-002", "Active", isFaculty, new HashSet<>(Set.of(lecturerRole, committeeRole)), 0L);
        seedUser("hoidong_pham", "pham.vd@euni.edu.vn", "TS. Phạm Văn D", "password", "HD-001", "Active", fitFaculty, new HashSet<>(Set.of(councilRole)), 0L);
        seedUser("cv_dbcl", "dbcl@euni.edu.vn", "Chuyên viên ĐBCL", "password", "CL-001", "Active", fitFaculty, new HashSet<>(Set.of(qaRole)), 0L);

        log.info("Base database seeding completed successfully with 8 roles.");
    }

    private Faculty seedFaculty(String name, String code) {
        return facultyRepository.findByCode(code).orElseGet(() -> {
            Faculty faculty = new Faculty();
            faculty.setName(name);
            faculty.setCode(code);
            return facultyRepository.save(faculty);
        });
    }

    private Permission seedPermission(String name, String code, String description) {
        return permissionRepository.findByCode(code).orElseGet(() -> {
            Permission perm = new Permission();
            perm.setName(name);
            perm.setCode(code);
            perm.setDescription(description);
            return permissionRepository.save(perm);
        });
    }

    private Role seedRole(String name, String code, Set<Permission> permissions) {
        return roleRepository.findByCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setCode(code);
            role.setPermissions(permissions);
            return roleRepository.save(role);
        });
    }

    private void seedUser(String username, String email, String fullName, String rawPassword, String employeeId, String status, Faculty faculty, Set<Role> roles, Long tokenVersion) {
        Optional<User> existingOpt = userRepository.findByUsernameOrEmailOrEmployeeId(username, email, employeeId);
        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();
            existing.setUsername(username);
            existing.setEmail(email);
            existing.setFullName(fullName);
            if (employeeId != null) {
                existing.setEmployeeId(employeeId);
            }
            existing.setStatus(status);
            existing.setFaculty(faculty);
            existing.setRoles(roles);
            userRepository.save(existing);
        } else {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setEmployeeId(employeeId);
            user.setStatus(status);
            user.setFaculty(faculty);
            user.setRoles(roles);
            user.setTokenVersion(tokenVersion != null ? tokenVersion : 0L);
            userRepository.save(user);
        }
    }
}
