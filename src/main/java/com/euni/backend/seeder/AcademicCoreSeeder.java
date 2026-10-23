package com.euni.backend.seeder;

import com.euni.backend.entity.*;
import com.euni.backend.entity.enums.BloomLevel;
import com.euni.backend.entity.enums.DomainType;
import com.euni.backend.entity.enums.ProgramStatus;
import com.euni.backend.repository.*;
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
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final ProgramStructureRepository programStructureRepository;
    private final ProgramElectiveGroupRepository programElectiveGroupRepository;
    private final ProgramObjectiveRepository programObjectiveRepository;
    private final ProgramLearningOutcomeRepository programLearningOutcomeRepository;
    private final PerformanceIndicatorRepository performanceIndicatorRepository;
    private final ObjectivePiMapRepository objectivePiMapRepository;
    private final CoursePiMapRepository coursePiMapRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting safe academic core seeding (natural key existence checks)...");

        // 1. Faculties
        Faculty fitFaculty = facultyRepository.findByCode("IT_FACULTY")
                .orElseGet(() -> facultyRepository.save(Faculty.builder().name("Khoa Công nghệ Thông tin").code("IT_FACULTY").build()));
        Faculty seFaculty = facultyRepository.findByCode("SE_FACULTY")
                .orElseGet(() -> facultyRepository.save(Faculty.builder().name("Khoa Kỹ thuật Phần mềm").code("SE_FACULTY").build()));

        // 2. Departments (Bộ môn)
        Department deptCnpm = seedDepartment("Bộ môn Công nghệ phần mềm", "CNPM", "Chịu trách nhiệm giảng dạy quy trình phần mềm, kiến trúc & kiểm thử", seFaculty);
        Department deptHttt = seedDepartment("Bộ môn Hệ thống thông tin", "HTTT", "Chịu trách nhiệm giảng dạy CSDL, phân tích thiết kế & ERP", fitFaculty);

        // 3. Majors (Ngành học kèm Department)
        Major seMajor = seedMajor("Kỹ thuật Phần mềm", "SE", "Đào tạo chuyên sâu quy trình phát triển & sản xuất phần mềm", seFaculty, deptCnpm);
        Major csMajor = seedMajor("Khoa học Máy tính", "CS", "Đào tạo nền tảng thuật toán, tính toán và AI", fitFaculty, null);
        Major aiMajor = seedMajor("Trí tuệ Nhân tạo & Data Science", "AI", "Đào tạo chuyên sâu Mô hình học máy, Deep Learning & Big Data", fitFaculty, null);
        Major isMajor = seedMajor("Hệ thống Thông tin Quản lý", "IS", "Đào tạo về CSDL, Chuyển đổi số & Enterprise Architect", fitFaculty, deptHttt);
        Major ceMajor = seedMajor("Kỹ thuật Máy tính & IoT", "CE", "Đào tạo Phần cứng, Nhúng & Internet of Things", fitFaculty, null);

        // 4. Programs
        Program seClcProg = seedProgram(
                "Chương trình Kỹ thuật phần mềm Chất lượng cao 2026", "CTDT-SE-CLC-2026",
                "Chương trình đào tạo theo chuẩn kiểm định quốc tế AUN-QA", seMajor, ProgramStatus.ACTIVE,
                2026, 150, "ENGINEER", "FULL_TIME"
        );

        Program seDtProg = seedProgram(
                "Chương trình Kỹ thuật phần mềm Đại trà 2026", "CTDT-SE-DT-2026",
                "Chương trình đào tạo chính quy chuẩn quốc gia", seMajor, ProgramStatus.DRAFT,
                2026, 140, "ENGINEER", "FULL_TIME"
        );

        Program csClcProg = seedProgram(
                "Chương trình Khoa học máy tính Chất lượng cao 2025", "CTDT-CS-CLC-2025",
                "Chương trình tập trung vào thuật toán và học máy", csMajor, ProgramStatus.ACTIVE,
                2025, 145, "BACHELOR", "FULL_TIME"
        );

        Program aiProg = seedProgram(
                "Chương trình Trí tuệ Nhân tạo & Data Science 2026", "CTDT-AI-2026",
                "Chương trình tiên phong về AI & Big Data Analytics", aiMajor, ProgramStatus.ACTIVE,
                2026, 148, "ENGINEER", "FULL_TIME"
        );

        // 5. Seed Courses
        Course c1 = seedCourse("Lập trình Hướng đối tượng", "IT001", 4, "Kiến thức Java, C++, OOP Principles.");
        Course c2 = seedCourse("Cấu trúc Dữ liệu và Giải thuật", "IT002", 4, "Array, LinkedList, Tree, Graph, Sorting.");
        Course c3 = seedCourse("Cơ sở Dữ liệu & Hệ quản trị CSDL", "IT003", 3, "SQL, Relational Algebra, ERD, PostgreSQL.");
        Course c4 = seedCourse("Mạng Máy tính & Truyền thông", "IT004", 3, "Mô hình OSI, TCP/IP, Routing, Socket.");
        Course c5 = seedCourse("Kiến trúc & Thiết kế Phần mềm", "IT005", 3, "Design Patterns, Microservices, DDD, MVC.");
        Course c6 = seedCourse("Kỹ nghệ Yêu cầu Phần mềm", "IT006", 3, "User Stories, Use Case Diagrams, System Specification.");
        Course c7 = seedCourse("Khảo sát & Phát triển Chương trình Đào tạo", "IT007", 3, "Quy trình xây dựng CTĐT, chuẩn đầu ra OBE.");
        Course c8 = seedCourse("Kiểm thử & Đảm bảo Chất lượng Phần mềm", "IT008", 3, "Unit Testing, Integration Testing, Automation Testing.");
        Course c9 = seedCourse("Trí tuệ Nhân tạo & Học máy", "IT009", 4, "Supervised/Unsupervised Learning, Neural Networks.");
        Course c10 = seedCourse("An toàn & Bảo mật Thông tin", "IT010", 3, "Mật mã học (RSA, AES), OWASP Top 10, Auth.");
        Course c11 = seedCourse("Lập trình Web Phân tán (Fullstack)", "IT011", 4, "Next.js, React, Node.js, Spring Boot, Docker.");
        Course c12 = seedCourse("Lập trình Ứng dụng Di động", "IT012", 3, "React Native, Flutter, Jetpack Compose.");
        Course c13 = seedCourse("Hệ quản trị CSDL Phân tán & Big Data", "IT013", 3, "NoSQL (MongoDB, Redis), Hadoop, Spark.");
        Course c14 = seedCourse("Quản lý Dự án Phần mềm", "IT014", 3, "Phương pháp Agile, Scrum, Kanban, JIRA.");
        Course c15 = seedCourse("Thực tập Doanh nghiệp (SE Internship)", "IT015", 4, "Thực tập thực tế 3 tháng tại công ty phần mềm.");
        Course c16 = seedCourse("Đồ án Tốt nghiệp Kỹ sư Phần mềm", "IT016", 10, "Xây dựng sản phẩm hoàn chỉnh và bảo vệ hội đồng.");

        // 6. Cây Cấu trúc khối kiến thức cho CTDT-SE-CLC-2026
        if (seClcProg != null) {
            ProgramStructure sDaiCuong = seedStructure(seClcProg, null, "STR-DC", "Khối kiến thức Giáo dục Đại cương", 24, 1);
            ProgramStructure sCoSoNganh = seedStructure(seClcProg, null, "STR-CSN", "Khối kiến thức Cơ sở ngành", 36, 2);
            ProgramStructure sChuyenNganh = seedStructure(seClcProg, null, "STR-CN", "Khối kiến thức Chuyên ngành", 42, 3);

            // Khối con Chuyên ngành
            ProgramStructure sCnBatBuoc = seedStructure(seClcProg, sChuyenNganh, "STR-CN-BB", "Chuyên ngành Bắt buộc", 30, 1);
            ProgramStructure sCnTuChon = seedStructure(seClcProg, sChuyenNganh, "STR-CN-TC", "Chuyên ngành Tự chọn", 12, 2);

            // Nhóm tự chọn (Elective Groups)
            ProgramElectiveGroup grpAi = seedElectiveGroup(sCnTuChon, "Hướng Chuyên sâu Trí tuệ Nhân tạo & Xử lý Dữ liệu", 6, 2);
            ProgramElectiveGroup grpDevOps = seedElectiveGroup(sCnTuChon, "Hướng Chuyên sâu Điện toán Đám mây & DevOps", 6, 2);

            // 7. Gán môn học vào Cấu trúc CTĐT
            ProgramCourse pc1 = assignCourse(seClcProg, c1, 1, true, sDaiCuong, null);
            ProgramCourse pc2 = assignCourse(seClcProg, c2, 2, true, sCoSoNganh, null);
            ProgramCourse pc3 = assignCourse(seClcProg, c3, 2, true, sCoSoNganh, null);
            ProgramCourse pc4 = assignCourse(seClcProg, c4, 3, true, sCoSoNganh, null);
            ProgramCourse pc5 = assignCourse(seClcProg, c5, 4, true, sCnBatBuoc, null);
            ProgramCourse pc6 = assignCourse(seClcProg, c6, 3, true, sCoSoNganh, null);
            ProgramCourse pc7 = assignCourse(seClcProg, c7, 4, true, sCnBatBuoc, null);
            ProgramCourse pc8 = assignCourse(seClcProg, c8, 5, true, sCnBatBuoc, null);
            ProgramCourse pc9 = assignCourse(seClcProg, c9, 5, false, sCnTuChon, grpAi);
            ProgramCourse pc10 = assignCourse(seClcProg, c10, 6, true, sCnBatBuoc, null);
            ProgramCourse pc11 = assignCourse(seClcProg, c11, 4, true, sCnBatBuoc, null);
            ProgramCourse pc12 = assignCourse(seClcProg, c12, 5, false, sCnTuChon, grpDevOps);
            ProgramCourse pc13 = assignCourse(seClcProg, c13, 6, false, sCnTuChon, grpAi);
            ProgramCourse pc14 = assignCourse(seClcProg, c14, 6, true, sCnBatBuoc, null);
            ProgramCourse pc15 = assignCourse(seClcProg, c15, 7, true, sCnBatBuoc, null);
            ProgramCourse pc16 = assignCourse(seClcProg, c16, 8, true, sCnBatBuoc, null);

            // 8. Seed OBE: 3 POs, 5 PLOs, 12 PIs
            ProgramObjective po1 = seedObjective(seClcProg, "PO1", "Có kiến thức nền tảng vững chắc và chuyên sâu về kỹ thuật phần mềm.", 1);
            ProgramObjective po2 = seedObjective(seClcProg, "PO2", "Có khả năng thiết kế, vận hành và quản lý dự án phần mềm quy mô lớn.", 2);
            ProgramObjective po3 = seedObjective(seClcProg, "PO3", "Có đạo đức nghề nghiệp, trách nhiệm xã hội và năng lực hội nhập quốc tế.", 3);

            // PLO 1 -> 5
            ProgramLearningOutcome plo1 = seedPlo(seClcProg, "PLO1", "Nền tảng Toán & Kỹ thuật", "Áp dụng kiến thức toán học và khoa học máy tính vào xây dựng phần mềm.", DomainType.KNOWLEDGE, 1);
            ProgramLearningOutcome plo2 = seedPlo(seClcProg, "PLO2", "Thiết kế & Kiến trúc Phần mềm", "Thiết kế kiến trúc hệ thống phần mềm đáp ứng yêu cầu phức tạp.", DomainType.SKILL, 2);
            ProgramLearningOutcome plo3 = seedPlo(seClcProg, "PLO3", "Kiểm thử & Đảm bảo chất lượng", "Xây dựng chiến lược kiểm thử tự động và quản lý chất lượng phần mềm.", DomainType.SKILL, 3);
            ProgramLearningOutcome plo4 = seedPlo(seClcProg, "PLO4", "Quản lý Dự án & Làm việc nhóm", "Áp dụng Agile/Scrum và phối hợp hiệu quả trong nhóm đa ngành.", DomainType.AUTONOMY, 4);
            ProgramLearningOutcome plo5 = seedPlo(seClcProg, "PLO5", "Đạo đức & Trách nhiệm Xã hội", "Tuân thủ pháp luật, đạo đức nghề nghiệp và cam kết học tập suốt đời.", DomainType.ATTITUDE, 5);

            // PIs
            PerformanceIndicator pi1_1 = seedPi(plo1, "PI1.1", "Giải thích các khái niệm thuật toán và độ phức tạp tính toán.", BloomLevel.UNDERSTAND, 1);
            PerformanceIndicator pi1_2 = seedPi(plo1, "PI1.2", "Cài đặt thành thạo các cấu trúc dữ liệu và giải thuật căn bản.", BloomLevel.APPLY, 2);

            PerformanceIndicator pi2_1 = seedPi(plo2, "PI2.1", "Phân tích và mô hình hóa yêu cầu hệ thống.", BloomLevel.ANALYZE, 1);
            PerformanceIndicator pi2_2 = seedPi(plo2, "PI2.2", "Thiết kế kiến trúc hướng dịch vụ (Microservices, DDD).", BloomLevel.CREATE, 2);
            PerformanceIndicator pi2_3 = seedPi(plo2, "PI2.3", "Hiện thực hóa mã nguồn an toàn và tối ưu hiệu năng.", BloomLevel.APPLY, 3);

            PerformanceIndicator pi3_1 = seedPi(plo3, "PI3.1", "Lập kế hoạch kiểm thử đơn vị và tích hợp.", BloomLevel.APPLY, 1);
            PerformanceIndicator pi3_2 = seedPi(plo3, "PI3.2", "Phát hiện và sửa lỗi bảo mật theo tiêu chuẩn OWASP.", BloomLevel.ANALYZE, 2);

            PerformanceIndicator pi4_1 = seedPi(plo4, "PI4.1", "Sử dụng các công cụ quản lý dự án Agile/Scrum.", BloomLevel.APPLY, 1);
            PerformanceIndicator pi4_2 = seedPi(plo4, "PI4.2", "Trình bày giải pháp kỹ thuật trước hội đồng hoặc khách hàng.", BloomLevel.APPLY, 2);

            PerformanceIndicator pi5_1 = seedPi(plo5, "PI5.1", "Tuân thủ quy định sở hữu trí tuệ và bảo mật dữ liệu cá nhân.", BloomLevel.UNDERSTAND, 1);
            PerformanceIndicator pi5_2 = seedPi(plo5, "PI5.2", "Chủ động cập nhật công nghệ mới và tự học nghiên cứu.", BloomLevel.EVALUATE, 2);

            // 9. Ma trận PO <-> PI
            seedObjectivePiMap(po1, pi1_1, "Trọng tâm kiến thức");
            seedObjectivePiMap(po1, pi1_2, "Trọng tâm kỹ năng thuật toán");
            seedObjectivePiMap(po1, pi2_3, "Kỹ năng lập trình");
            seedObjectivePiMap(po2, pi2_1, "Phân tích yêu cầu");
            seedObjectivePiMap(po2, pi2_2, "Kiến trúc hệ thống");
            seedObjectivePiMap(po2, pi3_1, "Kiểm thử chất lượng");
            seedObjectivePiMap(po2, pi4_1, "Quản lý Scrum");
            seedObjectivePiMap(po3, pi5_1, "Đạo đức nghề nghiệp");
            seedObjectivePiMap(po3, pi5_2, "Học tập suốt đời");

            // 10. Ma trận đóng góp môn học Course <-> PI (1-I, 2-T, 3-U)
            seedCoursePiMap(pc1, pi1_2, 2); // IT001 -> PI1.2 (T)
            seedCoursePiMap(pc2, pi1_1, 2); // IT002 -> PI1.1 (T)
            seedCoursePiMap(pc2, pi1_2, 3); // IT002 -> PI1.2 (U)
            seedCoursePiMap(pc3, pi2_3, 2); // IT003 -> PI2.3 (T)
            seedCoursePiMap(pc5, pi2_2, 3); // IT005 -> PI2.2 (U)
            seedCoursePiMap(pc6, pi2_1, 3); // IT006 -> PI2.1 (U)
            seedCoursePiMap(pc8, pi3_1, 3); // IT008 -> PI3.1 (U)
            seedCoursePiMap(pc10, pi3_2, 3); // IT010 -> PI3.2 (U)
            seedCoursePiMap(pc11, pi2_3, 3); // IT011 -> PI2.3 (U)
            seedCoursePiMap(pc14, pi4_1, 3); // IT014 -> PI4.1 (U)
            seedCoursePiMap(pc15, pi4_2, 2); // IT015 -> PI4.2 (T)
            seedCoursePiMap(pc16, pi4_2, 3); // IT016 -> PI4.2 (U)
            seedCoursePiMap(pc16, pi5_1, 2); // IT016 -> PI5.1 (T)
            seedCoursePiMap(pc16, pi5_2, 3); // IT016 -> PI5.2 (U)
        }

        log.info("Academic core seeding completed successfully.");
    }

    private Department seedDepartment(String name, String code, String description, Faculty faculty) {
        return departmentRepository.findByCode(code).orElseGet(() -> {
            Department d = Department.builder()
                    .name(name)
                    .code(code)
                    .description(description)
                    .faculty(faculty)
                    .build();
            return departmentRepository.save(d);
        });
    }

    private Major seedMajor(String name, String code, String description, Faculty faculty, Department department) {
        return majorRepository.findByCode(code).orElseGet(() -> {
            Major major = Major.builder()
                    .name(name)
                    .code(code)
                    .description(description)
                    .faculty(faculty)
                    .department(department)
                    .build();
            return majorRepository.save(major);
        });
    }

    private Program seedProgram(String name, String code, String description, Major major,
                                ProgramStatus status, Integer effectiveYear, Integer totalCredits,
                                String degreeLevel, String trainingMode) {
        return programRepository.findByCode(code).orElseGet(() -> {
            Program program = Program.builder()
                    .name(name)
                    .code(code)
                    .description(description)
                    .major(major)
                    .status(status)
                    .effectiveYear(effectiveYear)
                    .totalCredits(totalCredits)
                    .degreeLevel(degreeLevel)
                    .trainingMode(trainingMode)
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

    private ProgramStructure seedStructure(Program program, ProgramStructure parent, String code, String name, int creditRequired, int orderIndex) {
        return programStructureRepository.findByProgramIdAndCode(program.getId(), code).orElseGet(() -> {
            ProgramStructure s = ProgramStructure.builder()
                    .program(program)
                    .parent(parent)
                    .code(code)
                    .name(name)
                    .creditRequired(creditRequired)
                    .orderIndex(orderIndex)
                    .build();
            return programStructureRepository.save(s);
        });
    }

    private ProgramElectiveGroup seedElectiveGroup(ProgramStructure structure, String name, int minCredits, int minCourses) {
        return programElectiveGroupRepository.findByStructureIdAndName(structure.getId(), name).orElseGet(() -> {
            ProgramElectiveGroup eg = ProgramElectiveGroup.builder()
                    .structure(structure)
                    .name(name)
                    .minCredits(minCredits)
                    .minCourses(minCourses)
                    .build();
            return programElectiveGroupRepository.save(eg);
        });
    }

    private ProgramCourse assignCourse(Program program, Course course, int semester, boolean required,
                                      ProgramStructure structure, ProgramElectiveGroup electiveGroup) {
        if (program == null || course == null) return null;
        return programCourseRepository.findByProgramIdAndCourseId(program.getId(), course.getId())
                .map(pc -> {
                    pc.setStructure(structure);
                    pc.setElectiveGroup(electiveGroup);
                    pc.setSemester(semester);
                    pc.setRequired(required);
                    return programCourseRepository.save(pc);
                })
                .orElseGet(() -> {
                    ProgramCourse pc = ProgramCourse.builder()
                            .program(program)
                            .course(course)
                            .structure(structure)
                            .electiveGroup(electiveGroup)
                            .semester(semester)
                            .required(required)
                            .build();
                    return programCourseRepository.save(pc);
                });
    }

    private ProgramObjective seedObjective(Program program, String code, String content, int orderIndex) {
        return programObjectiveRepository.findByProgramIdAndCode(program.getId(), code).orElseGet(() -> {
            ProgramObjective po = ProgramObjective.builder()
                    .program(program)
                    .code(code)
                    .content(content)
                    .orderIndex(orderIndex)
                    .build();
            return programObjectiveRepository.save(po);
        });
    }

    private ProgramLearningOutcome seedPlo(Program program, String code, String name, String content, DomainType domain, int orderIndex) {
        return programLearningOutcomeRepository.findByProgramIdAndCode(program.getId(), code).orElseGet(() -> {
            ProgramLearningOutcome plo = ProgramLearningOutcome.builder()
                    .program(program)
                    .code(code)
                    .name(name)
                    .content(content)
                    .domain(domain)
                    .orderIndex(orderIndex)
                    .build();
            return programLearningOutcomeRepository.save(plo);
        });
    }

    private PerformanceIndicator seedPi(ProgramLearningOutcome plo, String code, String content, BloomLevel bloomLevel, int orderIndex) {
        return performanceIndicatorRepository.findByOutcomeIdAndCode(plo.getId(), code).orElseGet(() -> {
            PerformanceIndicator pi = PerformanceIndicator.builder()
                    .outcome(plo)
                    .code(code)
                    .content(content)
                    .bloomLevel(bloomLevel)
                    .orderIndex(orderIndex)
                    .build();
            return performanceIndicatorRepository.save(pi);
        });
    }

    private void seedObjectivePiMap(ProgramObjective po, PerformanceIndicator pi, String note) {
        objectivePiMapRepository.findByObjectiveIdAndPiId(po.getId(), pi.getId()).orElseGet(() -> {
            ObjectivePiMap map = ObjectivePiMap.builder()
                    .objective(po)
                    .performanceIndicator(pi)
                    .note(note)
                    .build();
            return objectivePiMapRepository.save(map);
        });
    }

    private void seedCoursePiMap(ProgramCourse pc, PerformanceIndicator pi, int level) {
        coursePiMapRepository.findByProgramCourseIdAndPiId(pc.getId(), pi.getId()).orElseGet(() -> {
            CoursePiMap map = CoursePiMap.builder()
                    .programCourse(pc)
                    .performanceIndicator(pi)
                    .level(level)
                    .build();
            return coursePiMapRepository.save(map);
        });
    }
}
