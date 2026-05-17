package com.euni.backend.seeder;

import com.euni.backend.entity.Course;
import com.euni.backend.entity.Faculty;
import com.euni.backend.entity.Major;
import com.euni.backend.entity.Program;
import com.euni.backend.entity.enums.ProgramStatus;
import com.euni.backend.repository.CourseRepository;
import com.euni.backend.repository.FacultyRepository;
import com.euni.backend.repository.MajorRepository;
import com.euni.backend.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after base data if any
@ConditionalOnProperty(name = "app.seeder.academic.enabled", havingValue = "true")
public class AcademicCoreSeeder implements CommandLineRunner {

    private final FacultyRepository facultyRepository;
    private final MajorRepository majorRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting academic core seeding...");

        // 1. Get existing faculty or use a default one (from DatabaseSeeder)
        Faculty itDept = facultyRepository.findByCode("IT_FACULTY")
                .orElse(null);

        if (itDept == null) {
            log.warn("IT_FACULTY not found, skipping major seeding.");
            return;
        }

        // 2. Seed Majors
        Major seMajor = seedMajor(UUID.fromString("a1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c71"), "Kỹ thuật phần mềm", "SE", "Ngành đào tạo chuyên sâu về quy trình sản xuất phần mềm", itDept);
        Major csMajor = seedMajor(UUID.fromString("a1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c72"), "Khoa học máy tính", "CS", "Ngành đào tạo về lý thuyết và nền tảng tính toán", itDept);

        // 3. Seed Programs for SE Major
        seedProgram(UUID.fromString("b1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c81"),
                "Chương trình Kỹ thuật phần mềm Chất lượng cao 2026", "CTDT-SE-CLC-2026",
                "Chương trình đào tạo theo chuẩn quốc tế", seMajor, ProgramStatus.ACTIVE,
                "Đào tạo kỹ sư phần mềm có năng lực toàn cầu", "Sinh viên có khả năng thiết kế hệ thống lớn", "PLO1, PLO2, PLO3");

        seedProgram(UUID.fromString("b1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c82"),
                "Chương trình Kỹ thuật phần mềm Đại trà 2026", "CTDT-SE-DT-2026",
                "Chương trình đào tạo chính quy", seMajor, ProgramStatus.DRAFT,
                "Đào tạo kỹ sư phần mềm chuyên nghiệp", "Nắm vững quy trình Agile", "PLO1, PLO2");

        // 4. Seed Courses
        seedCourse(UUID.fromString("d1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c91"), "Lập trình hướng đối tượng", "IT001", 4, "Học về Java và OOP principles");
        seedCourse(UUID.fromString("d1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c92"), "Cấu trúc dữ liệu và Giải thuật", "IT002", 4, "Học về thuật toán và cấu trúc dữ liệu");
        seedCourse(UUID.fromString("d1c2b3a4-5e6f-4a8b-9c0d-1e2f3a4b5c93"), "Cơ sở dữ liệu", "IT003", 3, "Học về SQL và thiết kế DB");

        log.info("Academic core seeding completed.");
    }

    private Major seedMajor(UUID id, String name, String code, String description, Faculty faculty) {
        return majorRepository.findByCode(code).orElseGet(() -> {
            Major major = Major.builder()
                    .id(id)
                    .name(name)
                    .code(code)
                    .description(description)
                    .faculty(faculty)
                    .build();
            return majorRepository.save(major);
        });
    }

    private void seedProgram(UUID id, String name, String code, String description, Major major,
                             ProgramStatus status, String generalObjective, String specificObjectives, String learningOutcomes) {
        if (!programRepository.existsByCode(code)) {
            Program program = Program.builder()
                    .id(id)
                    .name(name)
                    .code(code)
                    .description(description)
                    .major(major)
                    .status(status)
                    .generalObjective(generalObjective)
                    .specificObjectives(specificObjectives)
                    .learningOutcomes(learningOutcomes)
                    .build();
            programRepository.save(program);
        }
    }

    private void seedCourse(UUID id, String name, String code, int credits, String description) {
        if (!courseRepository.existsByCode(code)) {
            Course course = Course.builder()
                    .id(id)
                    .name(name)
                    .code(code)
                    .credits(credits)
                    .description(description)
                    .build();
            courseRepository.save(course);
        }
    }
}
