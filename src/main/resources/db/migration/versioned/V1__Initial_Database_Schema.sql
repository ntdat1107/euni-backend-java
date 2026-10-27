-- ============================================================================
-- V1__Initial_Database_Schema.sql
-- HỆ THỐNG QUẢN LÝ CHƯƠNG TRÌNH ĐÀO TẠO & ĐỀ CƯƠNG CHI TIẾT (EUNI CLM)
-- TỔNG HỢP TOÀN BỘ CSDL BAN ĐẦU (FULL COMPREHENSIVE INITIAL SCHEMA)
-- Tiêu chuẩn: Outcome-Based Education (OBE), Thông tư 17/2021/TT-BGDĐT, AUN-QA
-- Lưu ý kỹ thuật:
-- 1. Sắp xếp thứ tự tạo bảng nghiêm ngặt: Bảng cha được tạo trước bảng con.
-- 2. Mọi thực thể nghiệp vụ kế thừa BaseEntity đều có: created_at, updated_at, is_deleted.
-- 3. Đánh index tối ưu cho khóa ngoại, natural key và cờ is_deleted.
-- ============================================================================

-- ============================================================================
-- PHẦN 0: DỌN DẸP BẢNG CŨ (CLEANUP CASCADING)
-- ============================================================================
DROP TABLE IF EXISTS program_documents CASCADE;
DROP TABLE IF EXISTS program_evaluations CASCADE;
DROP TABLE IF EXISTS syllabus_assignments CASCADE;
DROP TABLE IF EXISTS committee_members CASCADE;
DROP TABLE IF EXISTS committees CASCADE;

DROP TABLE IF EXISTS clo_pi_maps CASCADE;
DROP TABLE IF EXISTS course_learning_outcomes CASCADE;
DROP TABLE IF EXISTS syllabuses CASCADE;
DROP TABLE IF EXISTS course_dependencies CASCADE;
DROP TABLE IF EXISTS course_pi_maps CASCADE;
DROP TABLE IF EXISTS objective_pi_maps CASCADE;
DROP TABLE IF EXISTS performance_indicators CASCADE;
DROP TABLE IF EXISTS program_learning_outcomes CASCADE;
DROP TABLE IF EXISTS program_objectives CASCADE;

DROP TABLE IF EXISTS survey_campaign_courses CASCADE;
DROP TABLE IF EXISTS survey_campaign_steps CASCADE;
DROP TABLE IF EXISTS survey_campaigns CASCADE;
DROP TABLE IF EXISTS workflow_step_definitions CASCADE;
DROP TABLE IF EXISTS workflow_template_drafts CASCADE;
DROP TABLE IF EXISTS workflow_templates CASCADE;

DROP TABLE IF EXISTS program_course_history CASCADE;
DROP TABLE IF EXISTS program_courses CASCADE;
DROP TABLE IF EXISTS program_elective_groups CASCADE;
DROP TABLE IF EXISTS program_structures CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS program_history CASCADE;
DROP TABLE IF EXISTS programs CASCADE;
DROP TABLE IF EXISTS majors CASCADE;
DROP TABLE IF EXISTS departments CASCADE;

DROP TABLE IF EXISTS user_ai_model_configs CASCADE;
DROP TABLE IF EXISTS user_ai_credentials CASCADE;
DROP TABLE IF EXISTS ai_prompt_templates CASCADE;

DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS faculties CASCADE;

-- Dọn dẹp sequences
DROP SEQUENCE IF EXISTS faculties_id_seq CASCADE;
DROP SEQUENCE IF EXISTS departments_id_seq CASCADE;
DROP SEQUENCE IF EXISTS users_id_seq CASCADE;
DROP SEQUENCE IF EXISTS roles_id_seq CASCADE;
DROP SEQUENCE IF EXISTS permissions_id_seq CASCADE;
DROP SEQUENCE IF EXISTS majors_id_seq CASCADE;
DROP SEQUENCE IF EXISTS programs_id_seq CASCADE;
DROP SEQUENCE IF EXISTS program_structures_id_seq CASCADE;
DROP SEQUENCE IF EXISTS program_elective_groups_id_seq CASCADE;
DROP SEQUENCE IF EXISTS courses_id_seq CASCADE;
DROP SEQUENCE IF EXISTS program_courses_id_seq CASCADE;
DROP SEQUENCE IF EXISTS program_history_id_seq CASCADE;
DROP SEQUENCE IF EXISTS program_course_history_id_seq CASCADE;
DROP SEQUENCE IF EXISTS course_dependencies_id_seq CASCADE;

DROP SEQUENCE IF EXISTS program_objectives_id_seq CASCADE;
DROP SEQUENCE IF EXISTS program_learning_outcomes_id_seq CASCADE;
DROP SEQUENCE IF EXISTS performance_indicators_id_seq CASCADE;
DROP SEQUENCE IF EXISTS objective_pi_maps_id_seq CASCADE;
DROP SEQUENCE IF EXISTS course_pi_maps_id_seq CASCADE;

DROP SEQUENCE IF EXISTS syllabuses_id_seq CASCADE;
DROP SEQUENCE IF EXISTS course_learning_outcomes_id_seq CASCADE;
DROP SEQUENCE IF EXISTS clo_pi_maps_id_seq CASCADE;

DROP SEQUENCE IF EXISTS committees_id_seq CASCADE;
DROP SEQUENCE IF EXISTS committee_members_id_seq CASCADE;
DROP SEQUENCE IF EXISTS syllabus_assignments_id_seq CASCADE;
DROP SEQUENCE IF EXISTS program_evaluations_id_seq CASCADE;
DROP SEQUENCE IF EXISTS program_documents_id_seq CASCADE;

DROP SEQUENCE IF EXISTS workflow_templates_id_seq CASCADE;
DROP SEQUENCE IF EXISTS workflow_template_drafts_id_seq CASCADE;
DROP SEQUENCE IF EXISTS workflow_step_definitions_id_seq CASCADE;
DROP SEQUENCE IF EXISTS survey_campaigns_id_seq CASCADE;
DROP SEQUENCE IF EXISTS survey_campaign_steps_id_seq CASCADE;
DROP SEQUENCE IF EXISTS survey_campaign_courses_id_seq CASCADE;

DROP SEQUENCE IF EXISTS ai_prompt_templates_id_seq CASCADE;
DROP SEQUENCE IF EXISTS user_ai_credentials_id_seq CASCADE;
DROP SEQUENCE IF EXISTS user_ai_model_configs_id_seq CASCADE;

-- ============================================================================
-- PHẦN 1: KHỞI TẠO SEQUENCES
-- ============================================================================
CREATE SEQUENCE faculties_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE departments_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE users_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE roles_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE permissions_id_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE majors_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE programs_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE program_structures_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE program_elective_groups_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE courses_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE program_courses_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE program_history_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE program_course_history_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE course_dependencies_id_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE program_objectives_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE program_learning_outcomes_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE performance_indicators_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE objective_pi_maps_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE course_pi_maps_id_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE syllabuses_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE course_learning_outcomes_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE clo_pi_maps_id_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE committees_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE committee_members_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE syllabus_assignments_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE program_evaluations_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE program_documents_id_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE workflow_templates_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE workflow_template_drafts_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE workflow_step_definitions_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE survey_campaigns_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE survey_campaign_steps_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE survey_campaign_courses_id_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE ai_prompt_templates_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE user_ai_credentials_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE user_ai_model_configs_id_seq START WITH 1 INCREMENT BY 1;

-- ============================================================================
-- PHẦN 2: CƠ CẤU TỔ CHỨC & PHÂN QUYỀN RBAC (ORGANIZATION & RBAC)
-- ============================================================================

-- 1. Khoa đào tạo (Faculties)
CREATE TABLE faculties (
    id BIGINT DEFAULT nextval('faculties_id_seq') PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_faculties_code ON faculties(code);
CREATE INDEX idx_faculties_deleted ON faculties(is_deleted);

-- 2. Bộ môn chuyên môn (Departments - trực thuộc Khoa)
CREATE TABLE departments (
    id BIGINT DEFAULT nextval('departments_id_seq') PRIMARY KEY,
    faculty_id BIGINT NOT NULL REFERENCES faculties(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_departments_faculty_id ON departments(faculty_id);
CREATE INDEX idx_departments_code ON departments(code);
CREATE INDEX idx_departments_deleted ON departments(is_deleted);

-- 3. Vai trò người dùng (Roles)
CREATE TABLE roles (
    id BIGINT DEFAULT nextval('roles_id_seq') PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_roles_code ON roles(code);
CREATE INDEX idx_roles_deleted ON roles(is_deleted);

-- 4. Quyền hạn thao tác (Permissions)
CREATE TABLE permissions (
    id BIGINT DEFAULT nextval('permissions_id_seq') PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_permissions_code ON permissions(code);
CREATE INDEX idx_permissions_deleted ON permissions(is_deleted);

-- 5. Tài khoản người dùng (Users)
CREATE TABLE users (
    id BIGINT DEFAULT nextval('users_id_seq') PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    employee_id VARCHAR(50) UNIQUE,
    faculty_id BIGINT REFERENCES faculties(id) ON DELETE SET NULL,
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'Inactive',
    token_version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_deleted ON users(is_deleted);

-- 6. Bảng nối Phân quyền Role - Permission
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 7. Bảng nối Gán vai trò User - Role
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================================
-- PHẦN 3: DANH MỤC HỌC THUẬT & CHƯƠNG TRÌNH ĐÀO TẠO (ACADEMIC CORE)
-- ============================================================================

-- 8. Ngành đào tạo (Majors - thuộc Khoa & Bộ môn phụ trách)
CREATE TABLE majors (
    id BIGINT DEFAULT nextval('majors_id_seq') PRIMARY KEY,
    faculty_id BIGINT REFERENCES faculties(id) ON DELETE SET NULL,
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_majors_code ON majors(code);
CREATE INDEX idx_majors_faculty_id ON majors(faculty_id);
CREATE INDEX idx_majors_department_id ON majors(department_id);
CREATE INDEX idx_majors_deleted ON majors(is_deleted);

-- 9. Chương trình đào tạo (Programs - hỗ trợ Versioning & Clone)
CREATE TABLE programs (
    id BIGINT DEFAULT nextval('programs_id_seq') PRIMARY KEY,
    major_id BIGINT NOT NULL REFERENCES majors(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES programs(id) ON DELETE SET NULL, -- Lưu vết kế thừa khi Rà soát/Clone
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, REVIEWING, APPROVED, ACTIVE, ARCHIVED
    effective_year INT NOT NULL DEFAULT 2026, -- Năm bắt đầu áp dụng (khóa tuyển sinh)
    total_credits INT NOT NULL DEFAULT 130, -- Tổng tín chỉ toàn khóa
    degree_level VARCHAR(50) DEFAULT 'BACHELOR', -- BACHELOR (Đại học), MASTER (Thạc sĩ), DOCTOR (Tiến sĩ)
    training_mode VARCHAR(50) DEFAULT 'FORMAL', -- FORMAL (Chính quy), WORK_STUDY (Vừa làm vừa học), DISTANCE (Từ xa)
    current_revision INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_programs_code ON programs(code);
CREATE INDEX idx_programs_major_id ON programs(major_id);
CREATE INDEX idx_programs_parent_id ON programs(parent_id);
CREATE INDEX idx_programs_status ON programs(status);
CREATE INDEX idx_programs_deleted ON programs(is_deleted);

-- 10. Cây cấu trúc khối kiến thức (Program Structures - Đệ quy phân cấp)
CREATE TABLE program_structures (
    id BIGINT DEFAULT nextval('program_structures_id_seq') PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES program_structures(id) ON DELETE CASCADE, -- Đệ quy nhóm cha - nhóm con
    code VARCHAR(50) NOT NULL, -- vd: GENERAL, PROFESSIONAL, CORE, SPECIALIZED
    name VARCHAR(255) NOT NULL,
    credit_required INT NOT NULL DEFAULT 0, -- Số tín chỉ tối thiểu sinh viên phải tích lũy trong khối
    order_index INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_program_structures_program_id ON program_structures(program_id);
CREATE INDEX idx_program_structures_parent_id ON program_structures(parent_id);
CREATE INDEX idx_program_structures_deleted ON program_structures(is_deleted);

-- 11. Nhóm môn tự chọn trong khối kiến thức (Program Elective Groups)
CREATE TABLE program_elective_groups (
    id BIGINT DEFAULT nextval('program_elective_groups_id_seq') PRIMARY KEY,
    structure_id BIGINT NOT NULL REFERENCES program_structures(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL, -- vd: "Hướng chuyên sâu Trí tuệ nhân tạo", "Tự chọn xã hội"
    min_credits INT NOT NULL DEFAULT 0, -- Chọn tối thiểu bao nhiêu tín chỉ
    min_courses INT NOT NULL DEFAULT 0, -- Hoặc chọn tối thiểu bao nhiêu môn
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_program_elective_groups_structure_id ON program_elective_groups(structure_id);
CREATE INDEX idx_program_elective_groups_deleted ON program_elective_groups(is_deleted);

-- 12. Danh mục môn học toàn trường (Courses - Course Catalog)
CREATE TABLE courses (
    id BIGINT DEFAULT nextval('courses_id_seq') PRIMARY KEY,
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL, -- Bộ môn quản lý chuyên môn
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    name_en VARCHAR(255),
    credits INT NOT NULL DEFAULT 3, -- Tổng số tín chỉ
    theory_credits INT NOT NULL DEFAULT 2, -- Tín chỉ lý thuyết (1 TC = 15 tiết)
    practice_credits INT NOT NULL DEFAULT 1, -- Tín chỉ thực hành (1 TC = 30-45 tiết)
    self_study_hours INT NOT NULL DEFAULT 90, -- Giờ tự học theo quy định
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_courses_code ON courses(code);
CREATE INDEX idx_courses_department_id ON courses(department_id);
CREATE INDEX idx_courses_deleted ON courses(is_deleted);

-- 13. Môn học được đưa vào CTĐT cụ thể (Program Courses)
CREATE TABLE program_courses (
    id BIGINT DEFAULT nextval('program_courses_id_seq') PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    structure_id BIGINT NOT NULL REFERENCES program_structures(id) ON DELETE CASCADE,
    elective_group_id BIGINT REFERENCES program_elective_groups(id) ON DELETE SET NULL,
    semester INT NOT NULL DEFAULT 1, -- Học kỳ dự kiến (1 - 9)
    is_required BOOLEAN NOT NULL DEFAULT TRUE, -- Môn bắt buộc hay tự chọn
    applied_syllabus_id BIGINT, -- Khóa ngoại trỏ tới bản Đề cương được áp dụng (gán sau khi tạo syllabuses)
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(program_id, course_id)
);
CREATE INDEX idx_program_courses_program_id ON program_courses(program_id);
CREATE INDEX idx_program_courses_course_id ON program_courses(course_id);
CREATE INDEX idx_program_courses_structure_id ON program_courses(structure_id);
CREATE INDEX idx_program_courses_deleted ON program_courses(is_deleted);

-- 14. Ràng buộc học phần trong CTĐT (Course Dependencies)
CREATE TABLE course_dependencies (
    id BIGINT DEFAULT nextval('course_dependencies_id_seq') PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    prerequisite_course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    dependency_type VARCHAR(20) NOT NULL DEFAULT 'PREREQUISITE', -- PREREQUISITE (Tiên quyết), PRE_COURSE (Học trước), COREQUISITE (Song hành)
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(program_id, course_id, prerequisite_course_id)
);
CREATE INDEX idx_course_dep_program ON course_dependencies(program_id);
CREATE INDEX idx_course_dep_course ON course_dependencies(course_id);
CREATE INDEX idx_course_dep_prereq ON course_dependencies(prerequisite_course_id);
CREATE INDEX idx_course_dep_deleted ON course_dependencies(is_deleted);

-- ============================================================================
-- PHẦN 4: LÕI CHUẨN ĐẦU RA OBE & MA TRẬN ĐÀO TẠO (PO - PLO - PI - MATRIX)
-- ============================================================================

-- 15. Mục tiêu đào tạo CTĐT (Program Objectives - PO / PEO)
CREATE TABLE program_objectives (
    id BIGINT DEFAULT nextval('program_objectives_id_seq') PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL, -- vd: PO1, PO2
    content TEXT NOT NULL,
    order_index INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(program_id, code)
);
CREATE INDEX idx_program_objectives_program ON program_objectives(program_id);
CREATE INDEX idx_program_objectives_deleted ON program_objectives(is_deleted);

-- 16. Chuẩn đầu ra cấp chương trình (Program Learning Outcomes - PLO / SO)
CREATE TABLE program_learning_outcomes (
    id BIGINT DEFAULT nextval('program_learning_outcomes_id_seq') PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL, -- vd: PLO1, PLO2
    name VARCHAR(255),
    content TEXT NOT NULL,
    domain VARCHAR(50) NOT NULL DEFAULT 'KNOWLEDGE', -- KNOWLEDGE (Kiến thức), SKILL (Kỹ năng), ATTITUDE (Tự chủ & trách nhiệm)
    order_index INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(program_id, code)
);
CREATE INDEX idx_program_learning_outcomes_program ON program_learning_outcomes(program_id);
CREATE INDEX idx_program_learning_outcomes_deleted ON program_learning_outcomes(is_deleted);

-- 17. Chỉ số đo lường thực hiện chi tiết (Performance Indicators - PI)
CREATE TABLE performance_indicators (
    id BIGINT DEFAULT nextval('performance_indicators_id_seq') PRIMARY KEY,
    outcome_id BIGINT NOT NULL REFERENCES program_learning_outcomes(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL, -- vd: PI1.1, PI1.2
    content TEXT NOT NULL,
    bloom_level VARCHAR(20) DEFAULT 'APPLY', -- REMEMBER, UNDERSTAND, APPLY, ANALYZE, EVALUATE, CREATE
    order_index INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(outcome_id, code)
);
CREATE INDEX idx_pi_outcome_id ON performance_indicators(outcome_id);
CREATE INDEX idx_pi_deleted ON performance_indicators(is_deleted);

-- 18. Ma trận liên kết Mục tiêu PO với Chỉ số PI (PO - PI Mapping)
CREATE TABLE objective_pi_maps (
    id BIGINT DEFAULT nextval('objective_pi_maps_id_seq') PRIMARY KEY,
    objective_id BIGINT NOT NULL REFERENCES program_objectives(id) ON DELETE CASCADE,
    pi_id BIGINT NOT NULL REFERENCES performance_indicators(id) ON DELETE CASCADE,
    note VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(objective_id, pi_id)
);
CREATE INDEX idx_obj_pi_obj ON objective_pi_maps(objective_id);
CREATE INDEX idx_obj_pi_pi ON objective_pi_maps(pi_id);
CREATE INDEX idx_obj_pi_deleted ON objective_pi_maps(is_deleted);

-- 19. Ma trận đóng góp của Học phần vào Chuẩn đầu ra CTĐT (Course - PI Mapping)
CREATE TABLE course_pi_maps (
    id BIGINT DEFAULT nextval('course_pi_maps_id_seq') PRIMARY KEY,
    program_course_id BIGINT NOT NULL REFERENCES program_courses(id) ON DELETE CASCADE,
    pi_id BIGINT NOT NULL REFERENCES performance_indicators(id) ON DELETE CASCADE,
    level INT NOT NULL DEFAULT 1, -- 1: Thấp (I - Introduce), 2: Trung bình (R - Reinforce), 3: Cao (M - Master)
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(program_course_id, pi_id)
);
CREATE INDEX idx_course_pi_pc ON course_pi_maps(program_course_id);
CREATE INDEX idx_course_pi_pi ON course_pi_maps(pi_id);
CREATE INDEX idx_course_pi_deleted ON course_pi_maps(is_deleted);

-- ============================================================================
-- PHẦN 5: ĐỀ CƯƠNG CHI TIẾT & CHUẨN ĐẦU RA HỌC PHẦN (SYLLABUS & CLO)
-- ============================================================================

-- 20. Đề cương chi tiết học phần theo phiên bản (Syllabuses)
CREATE TABLE syllabuses (
    id BIGINT DEFAULT nextval('syllabuses_id_seq') PRIMARY KEY,
    program_course_id BIGINT NOT NULL REFERENCES program_courses(id) ON DELETE CASCADE,
    version VARCHAR(20) NOT NULL DEFAULT '1.0', -- vd: 1.0, 2.0
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, REVIEWING, APPROVED, DEPRECATED
    course_description TEXT,
    course_objectives JSON, -- Muc tieu hoc phan (CO: kien thuc, ky nang, tu chu)
    teaching_methods TEXT,
    student_duties TEXT,
    assessment_scheme JSON, -- Rubric danh gia (chuyen can, giua ky, do an, cuoi ky)
    weekly_schedule JSON, -- Ke hoach giang day 15 tuan
    textbooks JSON, -- Giao trinh chinh & tai lieu tham khao
    facilities_requirements TEXT, -- Yeu cau ve phong may, phan mem, trang thiet bi
    is_master BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(program_course_id, version)
);
CREATE INDEX idx_syllabuses_pc_id ON syllabuses(program_course_id);
CREATE INDEX idx_syllabuses_status ON syllabuses(status);
CREATE INDEX idx_syllabuses_deleted ON syllabuses(is_deleted);

-- Bổ sung ràng buộc FK ngược từ program_courses vào syllabuses
ALTER TABLE program_courses
    ADD CONSTRAINT fk_pc_applied_syllabus
    FOREIGN KEY (applied_syllabus_id) REFERENCES syllabuses(id) ON DELETE SET NULL;

-- 21. Chuẩn đầu ra học phần (Course Learning Outcomes - CLO)
CREATE TABLE course_learning_outcomes (
    id BIGINT DEFAULT nextval('course_learning_outcomes_id_seq') PRIMARY KEY,
    syllabus_id BIGINT NOT NULL REFERENCES syllabuses(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL, -- vd: CLO1, CLO2
    content TEXT NOT NULL,
    bloom_level VARCHAR(20) DEFAULT 'APPLY',
    order_index INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(syllabus_id, code)
);
CREATE INDEX idx_clo_syllabus_id ON course_learning_outcomes(syllabus_id);
CREATE INDEX idx_clo_deleted ON course_learning_outcomes(is_deleted);

-- 22. Ma trận ánh xạ từ CLO lên Chỉ số đo lường PI của chương trình
CREATE TABLE clo_pi_maps (
    id BIGINT DEFAULT nextval('clo_pi_maps_id_seq') PRIMARY KEY,
    clo_id BIGINT NOT NULL REFERENCES course_learning_outcomes(id) ON DELETE CASCADE,
    pi_id BIGINT NOT NULL REFERENCES performance_indicators(id) ON DELETE CASCADE,
    weight DECIMAL(5,2) DEFAULT 1.0, -- Trọng số đóng góp
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(clo_id, pi_id)
);
CREATE INDEX idx_clo_pi_clo ON clo_pi_maps(clo_id);
CREATE INDEX idx_clo_pi_pi ON clo_pi_maps(pi_id);
CREATE INDEX idx_clo_pi_deleted ON clo_pi_maps(is_deleted);

-- ============================================================================
-- PHẦN 6: HỘI ĐỒNG, PHÂN CÔNG BIÊN SOẠN & MINH CHỨNG (GOVERNANCE & AUDIT)
-- ============================================================================

-- 23. Hội đồng CTĐT / Hội đồng thẩm định chuyên môn (Committees)
CREATE TABLE committees (
    id BIGINT DEFAULT nextval('committees_id_seq') PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    committee_type VARCHAR(50) NOT NULL DEFAULT 'DRAFTING', -- DRAFTING (Soạn thảo CTĐT), EVALUATION (Thẩm định khoa)
    decision_number VARCHAR(100), -- Số quyết định thành lập hội đồng
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_committees_program_id ON committees(program_id);
CREATE INDEX idx_committees_deleted ON committees(is_deleted);

-- 24. Thành viên hội đồng & Phân quyền ngữ cảnh (Committee Members)
CREATE TABLE committee_members (
    id BIGINT DEFAULT nextval('committee_members_id_seq') PRIMARY KEY,
    committee_id BIGINT NOT NULL REFERENCES committees(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER', -- PRESIDENT (Chủ tịch), SECRETARY (Thư ký), REVIEWER (Ủy viên phản biện), MEMBER (Ủy viên)
    can_edit BOOLEAN NOT NULL DEFAULT FALSE, -- Quyền chỉnh sửa trực tiếp nội dung CTĐT
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(committee_id, user_id)
);
CREATE INDEX idx_committee_members_committee ON committee_members(committee_id);
CREATE INDEX idx_committee_members_user ON committee_members(user_id);
CREATE INDEX idx_committee_members_deleted ON committee_members(is_deleted);

-- 25. Phân công đích danh giảng viên biên soạn đề cương chi tiết
CREATE TABLE syllabus_assignments (
    id BIGINT DEFAULT nextval('syllabus_assignments_id_seq') PRIMARY KEY,
    program_course_id BIGINT NOT NULL REFERENCES program_courses(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'PRIMARY_AUTHOR', -- PRIMARY_AUTHOR (Chủ biên), CONTRIBUTOR (Tham gia)
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(program_course_id, user_id)
);
CREATE INDEX idx_syl_assign_pc ON syllabus_assignments(program_course_id);
CREATE INDEX idx_syl_assign_user ON syllabus_assignments(user_id);
CREATE INDEX idx_syl_assign_deleted ON syllabus_assignments(is_deleted);

-- 26. Phiếu tự đánh giá mở ngành & Thẩm định chuyên môn (Program Evaluations)
CREATE TABLE program_evaluations (
    id BIGINT DEFAULT nextval('program_evaluations_id_seq') PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    form_type VARCHAR(50) NOT NULL, -- SELF_EVAL_DEAN (Trưởng khoa), COMMENT_RECTOR (Hiệu trưởng), COUNCIL_REVIEW (Hội đồng thẩm định)
    content TEXT,
    conclusion VARCHAR(50) DEFAULT 'QUALIFIED', -- QUALIFIED (Đạt), UNQUALIFIED (Chưa đạt), NEEDS_REVISION (Cần chỉnh sửa)
    is_locked BOOLEAN NOT NULL DEFAULT FALSE, -- Khóa không cho sửa khi đã nộp hoặc PĐT chốt
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_eval_program ON program_evaluations(program_id);
CREATE INDEX idx_eval_user ON program_evaluations(user_id);
CREATE INDEX idx_eval_deleted ON program_evaluations(is_deleted);

-- 27. Kho hồ sơ & Minh chứng kiểm định theo từng bước (Program Documents)
CREATE TABLE program_documents (
    id BIGINT DEFAULT nextval('program_documents_id_seq') PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    step_code VARCHAR(50) NOT NULL, -- vd: STEP_1 (Khảo sát), STEP_3 (QĐ tổ soạn thảo), STEP_8 (Biên bản thẩm định)
    doc_type VARCHAR(50) NOT NULL, -- DECISION, MEETING_MINUTES, BENCHMARK_REPORT, EVIDENCE
    title VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    uploaded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_prog_docs_program ON program_documents(program_id);
CREATE INDEX idx_prog_docs_step ON program_documents(step_code);
CREATE INDEX idx_prog_docs_deleted ON program_documents(is_deleted);

-- ============================================================================
-- PHẦN 7: QUY TRÌNH WORKFLOW CAMUNDA & CHIẾN DỊCH KHẢO SÁT (WORKFLOW & CAMPAIGN)
-- ============================================================================

-- 28. Mẫu quy trình BPMN thiết kế động (Workflow Templates)
CREATE TABLE workflow_templates (
    id BIGINT DEFAULT nextval('workflow_templates_id_seq') PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    json_content TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_wf_templates_code ON workflow_templates(code);
CREATE INDEX idx_wf_templates_deleted ON workflow_templates(is_deleted);

-- 29. Bản nháp quy trình BPMN (Workflow Template Drafts)
CREATE TABLE workflow_template_drafts (
    id BIGINT DEFAULT nextval('workflow_template_drafts_id_seq') PRIMARY KEY,
    template_id BIGINT REFERENCES workflow_templates(id) ON DELETE CASCADE,
    code VARCHAR(100),
    name VARCHAR(255),
    description TEXT,
    json_content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_saved_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_wf_drafts_template ON workflow_template_drafts(template_id);
CREATE INDEX idx_wf_drafts_deleted ON workflow_template_drafts(is_deleted);

-- 30. Định nghĩa bước nghiệp vụ tiêu chuẩn (Workflow Step Definitions)
CREATE TABLE workflow_step_definitions (
    id BIGINT DEFAULT nextval('workflow_step_definitions_id_seq') PRIMARY KEY,
    workflow_type VARCHAR(255) NOT NULL,
    step_code VARCHAR(255) NOT NULL UNIQUE,
    step_name VARCHAR(255) NOT NULL,
    type VARCHAR(255),
    required_documents TEXT,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_wf_step_defs_code ON workflow_step_definitions(step_code);
CREATE INDEX idx_wf_step_defs_deleted ON workflow_step_definitions(is_deleted);

-- 31. Chiến dịch khảo sát / Vận hành quy trình (Survey Campaigns)
CREATE TABLE survey_campaigns (
    id BIGINT DEFAULT nextval('survey_campaigns_id_seq') PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    workflow_template_id BIGINT NOT NULL REFERENCES workflow_templates(id),
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_survey_camp_code ON survey_campaigns(code);
CREATE INDEX idx_survey_camp_program ON survey_campaigns(program_id);
CREATE INDEX idx_survey_camp_deleted ON survey_campaigns(is_deleted);

-- 32. Các bước form tuần tự trong chiến dịch (Survey Campaign Steps)
CREATE TABLE survey_campaign_steps (
    id BIGINT DEFAULT nextval('survey_campaign_steps_id_seq') PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES survey_campaigns(id) ON DELETE CASCADE,
    step_index INT NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    deadline TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    required_documents TEXT,
    configuration TEXT,
    result_data TEXT, -- Lưu trữ dữ liệu biểu mẫu khảo sát (nếu có câu hỏi động)
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_survey_steps_camp ON survey_campaign_steps(campaign_id);
CREATE INDEX idx_survey_steps_deleted ON survey_campaign_steps(is_deleted);

-- ============================================================================
-- PHẦN 8: TRỢ LÝ AI & TÙY BIẾN MÔ HÌNH (AI PROMPTS & CREDENTIALS)
-- ============================================================================

-- 34. Mẫu Prompt chuyên môn AI (Ai Prompt Templates)
CREATE TABLE ai_prompt_templates (
    id BIGINT DEFAULT nextval('ai_prompt_templates_id_seq') PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    template_content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_ai_prompt_code ON ai_prompt_templates(code);
CREATE INDEX idx_ai_prompt_deleted ON ai_prompt_templates(is_deleted);

-- 35. Khóa cá nhân API AI - BYOK (User Ai Credentials)
CREATE TABLE user_ai_credentials (
    id BIGINT DEFAULT nextval('user_ai_credentials_id_seq') PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL, -- GEMINI, OPENAI, ANTHROPIC
    key_alias VARCHAR(100) NOT NULL,
    encrypted_api_key TEXT NOT NULL,
    encryption_iv VARCHAR(100) NOT NULL,
    base_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_user_ai_cred_user ON user_ai_credentials(user_id);
CREATE INDEX idx_user_ai_cred_deleted ON user_ai_credentials(is_deleted);

-- 36. Cấu hình Model AI cho từng tác vụ (User Ai Model Configs)
CREATE TABLE user_ai_model_configs (
    id BIGINT DEFAULT nextval('user_ai_model_configs_id_seq') PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_id BIGINT REFERENCES user_ai_credentials(id) ON DELETE SET NULL,
    task_type VARCHAR(50) NOT NULL, -- PEO_GEN, PLO_GEN, CLO_GEN, SYLLABUS_GEN, BENCHMARK
    model_name VARCHAR(100) NOT NULL,
    temperature DOUBLE PRECISION DEFAULT 0.5,
    max_tokens INTEGER DEFAULT 4096,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_user_ai_model_task UNIQUE(user_id, task_type)
);
CREATE INDEX idx_user_ai_model_user ON user_ai_model_configs(user_id);
CREATE INDEX idx_user_ai_model_deleted ON user_ai_model_configs(is_deleted);

-- ============================================================================
-- PHẦN 9: BẢNG LƯU VẾT LỊCH SỬ THAY ĐỔI (HISTORICAL AUDIT LOGS)
-- ============================================================================

-- 37. Lịch sử thay đổi thông tin CTĐT
CREATE TABLE program_history (
    id BIGINT DEFAULT nextval('program_history_id_seq') PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    name VARCHAR(255),
    code VARCHAR(50),
    description TEXT,
    revision_number INT NOT NULL,
    changed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    changed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    change_reason TEXT
);
CREATE INDEX idx_prog_hist_program ON program_history(program_id);

-- 38. Lịch sử thay đổi gán môn học vào CTĐT
CREATE TABLE program_course_history (
    id BIGINT DEFAULT nextval('program_course_history_id_seq') PRIMARY KEY,
    program_course_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    semester INT,
    is_required BOOLEAN,
    action VARCHAR(20) NOT NULL, -- ADDED, REMOVED, RESTORED, UPDATED
    changed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    changed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    change_reason TEXT
);
CREATE INDEX idx_pc_hist_program ON program_course_history(program_id);
CREATE INDEX idx_pc_hist_course ON program_course_history(course_id);

-- ============================================================================
-- PHẦN 10: SEED DỮ LIỆU BAN ĐẦU CỐT LÕI (SYSTEM PROMPTS SEED)
-- ============================================================================
INSERT INTO ai_prompt_templates (code, name, description, template_content, is_active)
VALUES 
('BASE_SYSTEM', 'Hướng dẫn hệ thống cơ bản', 'Quy tắc định dạng HTML và vai trò Chuyên gia Giáo dục của AI', 
'Bạn là Trợ lý AI Chuyên gia Giáo dục & Thiết kế Chương trình Đào tạo Đại học cho Hệ thống Quản lý eUni.
Nhiệm vụ của bạn là tạo nội dung văn bản HTML phù hợp để chèn trực tiếp vào trình biên soạn văn bản (Rich Text Editor).
QUY TẮC ĐỊNH DẠNG BẮT BUỘC:
- CHỈ trả về đoạn mã HTML hợp lệ (sử dụng các thẻ <p>, <ul>, <li>, <strong>, <em>, <h4>, <table>, <thead>, <tbody>, <tr>, <th>, <td>).
- KHÔNG bao bọc trong khối markdown ```html hoặc ```.
- Ngôn từ chuẩn mực học thuật, rõ ràng, dễ đo lường và theo đúng chuẩn kiểm định giáo dục (AUN-QA / MOET / Outcome-Based Education).', TRUE),

('PEO', 'Mục tiêu đào tạo chương trình (PO/PEO)', 'Mục tiêu chung và các mục tiêu cụ thể PO 1, PO 2,...',
'--- HƯỚNG DẪN CHUYÊN MÔN: MỤC TIÊU ĐÀO TẠO CHƯƠNG TRÌNH (PO / Program Objectives) ---
- Cấu trúc đầu ra bao gồm danh sách CÁC MỤC TIÊU CỤ THỂ (PO 1, PO 2, PO 3,...):
  <ul>
    <li><strong>PO 1:</strong> [Nội dung mục tiêu cụ thể 1]</li>
    <li><strong>PO 2:</strong> [Nội dung mục tiêu cụ thể 2]</li>
    <li><strong>PO 3:</strong> [Nội dung mục tiêu cụ thể 3]</li>
  </ul>
- Đảm bảo nội dung các PO tập trung vào năng lực phát triển sự nghiệp dài hạn (3-5 năm sau tốt nghiệp), trình độ chuyên môn, kỹ năng quản lý và đạo đức nghề nghiệp.', TRUE),

('CLO', 'Chuẩn đầu ra học phần (CLO)', 'Xây dựng CLO theo thang đo Bloom',
'--- HƯỚNG DẪN CHUYÊN MÔN: VIẾT CHUẨN ĐẦU RA HỌC PHẦN (CLO / Course Learning Outcomes) ---
- Sử dụng thang đo tư duy Bloom (Bloom''s Taxonomy) với các động từ hành động đo lường được (Ví dụ: Phân tích, Thiết kế, Đánh giá, Vận dụng, Giải quyết, Xây dựng).
- Cấu trúc từng CLO phải gồm: [Động từ Bloom] + [Đối tượng kiến thức/kỹ năng] + [Bối cảnh/Điều kiện áp dụng].
- Định dạng danh sách <ul><li><strong>CLO 1:</strong> ...</li><li><strong>CLO 2:</strong> ...</li></ul>.', TRUE),

('PLO', 'Chuẩn đầu ra chương trình (PLO & PI)', 'Cấu trúc PLO và chỉ số hiệu quả PI',
'--- HƯỚNG DẪN CHUYÊN MÔN: CHUẨN ĐẦU RA CHƯƠNG TRÌNH ĐÀO TẠO (PLO) & CHỈ SỐ PI ---
- Xây dựng chuẩn đầu ra tổng thể cấp chương trình đào tạo (Program Learning Outcomes - PLO) và chỉ số hiệu quả (Performance Indicators - PI).
- Tập trung vào các mảng kiến thức chuyên sâu, kỹ năng nghề nghiệp, khả năng giải quyết vấn đề phức tạp, đạo đức nghề nghiệp và khả năng học tập suốt đời.
- Định dạng cấu trúc: <ul><li><strong>PLO X:</strong> [Nội dung PLO]<ul><li><strong>PI X.1:</strong> [Chỉ số thành phần]</li></ul></li></ul>.', TRUE),

('SYLLABUS', 'Đề cương chi tiết học phần', 'Nội dung giảng dạy từng tuần / chương',
'--- HƯỚNG DẪN CHUYÊN MÔN: ĐỀ CƯƠNG CHI TIẾT HỌC PHẦN (Syllabus) ---
- Cấu trúc chương trình học hợp lý, chia theo các chương/tuần học logic từ cơ bản đến nâng cao.
- Mối chương cần nêu rõ: Tên chương, Chủ đề lý thuyết, Bài tập/Thực hành và Yêu cầu sinh viên tự học.
- Định dạng: Sử dụng <h4>Chương X: [Tên chương]</h4> kèm danh sách <ul><li>...</li></ul>.', TRUE),

('COURSE_DESCRIPTION', 'Mô tả tổng quan học phần', 'Tóm tắt vị trí và mục tiêu môn học',
'--- HƯỚNG DẪN CHUYÊN MÔN: MÔ TẢ TỔNG QUAN HỌC PHẦN ---
- Nêu vị trí môn học trong chương trình đào tạo, mục tiêu tổng quát, phương pháp tiếp cận và những giá trị cốt lõi sinh viên sẽ nhận được.
- Văn phong trang trọng, súc tích, truyền cảm hứng và chuẩn hóa học thuật.', TRUE),

('PREREQUISITES', 'Yêu cầu môn học tiên quyết', 'Môn học tiên quyết và kiến thức nền tảng',
'--- HƯỚNG DẪN CHUYÊN MÔN: YÊU CẦU MÔN HỌC TIÊN QUYẾT & KIẾN THỨC NỀN TẢNG ---
- Liệt kê các môn học tiên quyết (Prerequisite), môn học song hành (Corequisite) và khung kiến thức bắt buộc sinh viên cần trang bị trước khi bắt đầu môn học.', TRUE),

('ASSESSMENT', 'Phương pháp & Ma trận đánh giá', 'Hình thức đánh giá và trọng số %',
'--- HƯỚNG DẪN CHUYÊN MÔN: PHƯƠNG PHÁP & MA TRẬN ĐÁNH GIÁ HỌC PHẦN ---
- Thiết kế phương pháp đánh giá đa dạng: Chuyên cần, Đánh giá quá trình (Bài tập/Lab/Đồ án), Thi kết thúc học phần.
- Nếu được yêu cầu dạng bảng, hãy tạo <table> với các cột: Thành phần đánh giá, Hình thức đánh giá, Trọng số (%), Chuẩn đầu ra (CLO) tương ứng.', TRUE),

('DEFAULT', 'Hướng dẫn chung mặc định', 'Sử dụng khi không trùng khớp với các loại cụ thể',
'--- HƯỚNG DẪN CHUYÊN MÔN: NỘI DUNG HỌC THUẬT GIÁO DỤC ---
- Đảm bảo nội dung chuẩn hóa theo mô hình giáo dục đại học.', TRUE)
ON CONFLICT (code) DO NOTHING;
