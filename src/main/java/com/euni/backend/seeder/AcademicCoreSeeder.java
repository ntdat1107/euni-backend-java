package com.euni.backend.seeder;

import com.euni.backend.entity.Course;
import com.euni.backend.entity.Faculty;
import com.euni.backend.entity.Major;
import com.euni.backend.entity.Program;
import com.euni.backend.entity.ProgramCourse;
import com.euni.backend.entity.enums.ProgramStatus;
import com.euni.backend.repository.CourseRepository;
import com.euni.backend.repository.FacultyRepository;
import com.euni.backend.repository.MajorRepository;
import com.euni.backend.repository.ProgramCourseRepository;
import com.euni.backend.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
@ConditionalOnProperty(name = "app.seeder.academic.enabled", havingValue = "true", matchIfMissing = true)
public class AcademicCoreSeeder implements CommandLineRunner {

    private final FacultyRepository facultyRepository;
    private final MajorRepository majorRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final ProgramCourseRepository programCourseRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting safe academic core seeding (natural key existence checks)...");

        // 1. Get or seed Faculties
        Faculty fitFaculty = facultyRepository.findByCode("IT_FACULTY")
                .orElseGet(() -> facultyRepository.save(Faculty.builder().name("Khoa Công nghệ Thông tin").code("IT_FACULTY").build()));
        Faculty seFaculty = facultyRepository.findByCode("SE_FACULTY")
                .orElseGet(() -> facultyRepository.save(Faculty.builder().name("Khoa Kỹ thuật Phần mềm").code("SE_FACULTY").build()));

        // 2. Seed Majors
        Major seMajor = seedMajor("Kỹ thuật Phần mềm", "SE", "Đào tạo chuyên sâu quy trình phát triển & sản xuất phần mềm", seFaculty);
        Major csMajor = seedMajor("Khoa học Máy tính", "CS", "Đào tạo nền tảng thuật toán, tính toán và AI", fitFaculty);
        Major aiMajor = seedMajor("Trí tuệ Nhân tạo & Data Science", "AI", "Đào tạo chuyên sâu Mô hình học máy, Deep Learning & Big Data", fitFaculty);
        Major isMajor = seedMajor("Hệ thống Thông tin Quản lý", "IS", "Đào tạo về CSDL, Chuyển đổi số & Enterprise Architect", fitFaculty);
        Major ceMajor = seedMajor("Kỹ thuật Máy tính & IoT", "CE", "Đào tạo Phần cứng, Nhúng & Internet of Things", fitFaculty);

        // 3. Seed Programs
        Program seClcProg = seedProgram(
                "Chương trình Kỹ thuật phần mềm Chất lượng cao 2026", "CTDT-SE-CLC-2026",
                "Chương trình đào tạo theo chuẩn kiểm định quốc tế AUN-QA", seMajor, ProgramStatus.ACTIVE,
                "Đào tạo kỹ sư phần mềm có năng lực thiết kế, kiến trúc và quản lý dự án phần mềm quy mô lớn.",
                "1. Làm chủ quy trình Agile/Scrum. 2. Làm chủ Kiến trúc Microservices & Cloud. 3. Thành thạo DevOps & CI/CD.",
                "PLO1: Khả năng phân tích yêu cầu phần mềm. PLO2: Thiết kế Kiến trúc phần mềm. PLO3: Kiểm thử & Đảm bảo chất lượng. PLO4: Quản lý dự án Agile."
        );

        Program seDtProg = seedProgram(
                "Chương trình Kỹ thuật phần mềm Đại trà 2026", "CTDT-SE-DT-2026",
                "Chương trình đào tạo chính quy chuẩn quốc gia", seMajor, ProgramStatus.DRAFT,
                "Đào tạo kỹ sư phần mềm chuyên nghiệp phục vụ thị trường công nghệ.",
                "1. Nắm vững kỹ năng lập trình hướng đối tượng. 2. Xây dựng CSDL và ứng dụng Web/Mobile.",
                "PLO1: Lập trình thành thạo Java/Python. PLO2: Xây dựng CSDL Relational/NoSQL. PLO3: Làm việc nhóm."
        );

        Program csClcProg = seedProgram(
                "Chương trình Khoa học máy tính Chất lượng cao 2025", "CTDT-CS-CLC-2025",
                "Chương trình tập trung vào thuật toán và học máy", csMajor, ProgramStatus.ACTIVE,
                "Đào tạo các nhà khoa học máy tính có khả năng nghiên cứu và phát triển giải pháp AI.",
                "1. Nghiên cứu thuật toán tối ưu. 2. Phát triển mô hình Trí tuệ nhân tạo.",
                "PLO1: Toán rời rạc & Thuật toán. PLO2: Học máy & Xử lý ngôn ngữ tự nhiên. PLO3: Xử lý ảnh số."
        );

        Program aiProg = seedProgram(
                "Chương trình Trí tuệ Nhân tạo & Data Science 2026", "CTDT-AI-2026",
                "Chương trình tiên phong về AI & Big Data Analytics", aiMajor, ProgramStatus.ACTIVE,
                "Cung cấp nhân lực chất lượng cao trong lĩnh vực AI & Khoa học dữ liệu.",
                "1. Xây dựng pipeline dữ liệu lớn. 2. Huấn luyện mô hình Deep Learning.",
                "PLO1: Thu thập & Tiền xử lý dữ liệu. PLO2: Xây dựng mô hình Deep Learning. PLO3: Trực quan hóa dữ liệu."
        );

        // 4. Seed Courses (20 realistic subjects)
        Course c1 = seedCourse("Lập trình Hướng đối tượng", "IT001", 4, "Kiến thức Java, C++, OOP Principles: Abstraction, Encapsulation, Inheritance, Polymorphism.");
        Course c2 = seedCourse("Cấu trúc Dữ liệu và Giải thuật", "IT002", 4, "Array, LinkedList, Tree, Graph, Sorting & Searching Algorithms.");
        Course c3 = seedCourse("Cơ sở Dữ liệu & Hệ quản trị CSDL", "IT003", 3, "SQL, Relational Algebra, ERD, Normalization (1NF-3NF), PostgreSQL/MySQL.");
        Course c4 = seedCourse("Mạng Máy tính & Truyền thông", "IT004", 3, "Mô hình OSI, TCP/IP, Routing, Socket Programming, HTTP/HTTPS.");
        Course c5 = seedCourse("Kiến trúc & Thiết kế Phần mềm", "IT005", 3, "Design Patterns (Gang of Four), Microservices, Domain-Driven Design (DDD), MVC.");
        Course c6 = seedCourse("Kỹ nghệ Yêu cầu Phần mềm", "IT006", 3, "User Stories, Use Case Diagrams, System Specification, Prototyping.");
        Course c7 = seedCourse("Khảo sát & Phát triển Chương trình Đào tạo", "IT007", 3, "Quy trình xây dựng CTĐT, chuẩn đầu ra PLO/CLO, ma trận phát triển kỹ năng.");
        Course c8 = seedCourse("Kiểm thử & Đảm bảo Chất lượng Phần mềm", "IT008", 3, "Unit Testing, Integration Testing, Automation Testing (Selenium, Playwright), CI/CD.");
        Course c9 = seedCourse("Trí tuệ Nhân tạo & Học máy", "IT009", 4, "Supervised/Unsupervised Learning, Decision Trees, Neural Networks, PyTorch/Scikit-learn.");
        Course c10 = seedCourse("An toàn & Bảo mật Thông tin", "IT010", 3, "Mật mã học (RSA, AES), OWASP Top 10, Auth JWT/OAuth2, Vulnerability Scanning.");
        Course c11 = seedCourse("Lập trình Web Phân tán (Fullstack)", "IT011", 4, "Next.js, React, Node.js, Spring Boot, RESTful API, Docker.");
        Course c12 = seedCourse("Lập trình Ứng dụng Di động", "IT012", 3, "React Native, Flutter, Jetpack Compose, iOS/Android UI Components.");
        Course c13 = seedCourse("Hệ quản trị CSDL Phân tán & Big Data", "IT013", 3, "NoSQL (MongoDB, Redis), Hadoop, Apache Spark, Data Warehouse.");
        Course c14 = seedCourse("Quản lý Dự án Phần mềm", "IT014", 3, "Phương pháp Agile, Scrum, Kanban, JIRA, Ước lượng chi phí dự án.");
        Course c15 = seedCourse("Thực tập Doanh nghiệp (SE Internship)", "IT015", 4, "Thực tập thực tế 3 tháng tại các công ty phần mềm đối tác.");
        Course c16 = seedCourse("Đồ án Tốt nghiệp Kỹ sư Phần mềm", "IT016", 10, "Xây dựng sản phẩm phần mềm hoàn chỉnh và bảo vệ trước Hội đồng Khoa.");

        // 5. Assign Courses to Program (ProgramCourse mapping with semester numbers)
        if (seClcProg != null) {
            assignCourseToProgram(seClcProg, c1, 1);
            assignCourseToProgram(seClcProg, c2, 2);
            assignCourseToProgram(seClcProg, c3, 2);
            assignCourseToProgram(seClcProg, c4, 3);
            assignCourseToProgram(seClcProg, c5, 4);
            assignCourseToProgram(seClcProg, c6, 3);
            assignCourseToProgram(seClcProg, c7, 4);
            assignCourseToProgram(seClcProg, c8, 5);
            assignCourseToProgram(seClcProg, c9, 5);
            assignCourseToProgram(seClcProg, c10, 6);
            assignCourseToProgram(seClcProg, c11, 4);
            assignCourseToProgram(seClcProg, c12, 5);
            assignCourseToProgram(seClcProg, c13, 6);
            assignCourseToProgram(seClcProg, c14, 6);
            assignCourseToProgram(seClcProg, c15, 7);
            assignCourseToProgram(seClcProg, c16, 8);
        }

        log.info("Academic core seeding completed successfully.");
    }

    private Major seedMajor(String name, String code, String description, Faculty faculty) {
        return majorRepository.findByCode(code).orElseGet(() -> {
            Major major = Major.builder()
                    .name(name)
                    .code(code)
                    .description(description)
                    .faculty(faculty)
                    .build();
            return majorRepository.save(major);
        });
    }

    private Program seedProgram(String name, String code, String description, Major major,
                                ProgramStatus status, String generalObjective, String specificObjectives, String learningOutcomes) {
        return programRepository.findByCode(code).orElseGet(() -> {
            Program program = Program.builder()
                    .name(name)
                    .code(code)
                    .description(description)
                    .major(major)
                    .status(status)
                    .generalObjective(generalObjective)
                    .specificObjectives(specificObjectives)
                    .learningOutcomes(learningOutcomes)
                    .build();
            return programRepository.save(program);
        });
    }

    private Course seedCourse(String name, String code, int credits, String description) {
        return courseRepository.findByCode(code).orElseGet(() -> {
            Course course = Course.builder()
                    .name(name)
                    .code(code)
                    .credits(credits)
                    .description(description)
                    .build();
            return courseRepository.save(course);
        });
    }

    private void assignCourseToProgram(Program program, Course course, int semester) {
        if (program == null || course == null) return;
        programCourseRepository.findByProgramIdAndCourseId(program.getId(), course.getId())
                .orElseGet(() -> {
                    ProgramCourse pc = ProgramCourse.builder()
                            .program(program)
                            .course(course)
                            .semester(semester)
                            .build();
                    return programCourseRepository.save(pc);
                });
    }
}
