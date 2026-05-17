-- V20240510_01: Create Academic Core Tables
-- Author: Antigravity (AI Engineering Assistant)
-- Purpose: Schema for Majors, Programs, and Courses.

DROP TABLE IF EXISTS program_course_history CASCADE;
DROP TABLE IF EXISTS program_courses CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS program_history CASCADE;
DROP TABLE IF EXISTS programs CASCADE;
DROP TABLE IF EXISTS majors CASCADE;

-- 1. Majors Table (Ngành học)
CREATE TABLE majors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    faculty_id UUID REFERENCES faculties(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 2. Programs Table (Chương trình đào tạo)
CREATE TABLE programs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    major_id UUID REFERENCES majors(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'DRAFT',
    
    -- Extended fields from old source
    general_objective TEXT,
    specific_objectives TEXT,
    learning_outcomes TEXT,
    
    current_revision INT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 3. Courses Table (Học phần/Môn học)
CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    credits INT DEFAULT 3,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 4. Program-Courses Mapping
CREATE TABLE program_courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id UUID REFERENCES programs(id) ON DELETE CASCADE,
    course_id UUID REFERENCES courses(id) ON DELETE CASCADE,
    semester INT,
    is_required BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(program_id, course_id)
);

-- 5. Program History
CREATE TABLE program_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id UUID NOT NULL,
    name VARCHAR(255),
    code VARCHAR(50),
    description TEXT,
    general_objective TEXT,
    specific_objectives TEXT,
    learning_outcomes TEXT,
    revision_number INT NOT NULL,
    changed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    changed_by UUID,
    change_reason TEXT,
    CONSTRAINT fk_program_history_original FOREIGN KEY (program_id) REFERENCES programs(id) ON DELETE CASCADE
);

-- 6. Program-Course History (New: Traceability for mapping)
CREATE TABLE program_course_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_course_id UUID NOT NULL,
    program_id UUID NOT NULL,
    course_id UUID NOT NULL,
    semester INT,
    is_required BOOLEAN,
    action VARCHAR(20), -- ADDED, REMOVED, RESTORED, UPDATED
    changed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    changed_by UUID,
    change_reason TEXT
);

-- Performance Indexes
CREATE INDEX idx_majors_code ON majors(code);
CREATE INDEX idx_programs_code ON programs(code);
CREATE INDEX idx_courses_code ON courses(code);
CREATE INDEX idx_programs_major_id ON programs(major_id);
CREATE INDEX idx_program_courses_program_id ON program_courses(program_id);
CREATE INDEX idx_program_courses_course_id ON program_courses(course_id);
CREATE INDEX idx_pc_history_mapping_id ON program_course_history(program_course_id);
CREATE INDEX idx_pc_history_program_id ON program_course_history(program_id);
