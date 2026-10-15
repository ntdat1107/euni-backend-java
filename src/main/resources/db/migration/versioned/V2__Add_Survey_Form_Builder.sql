-- ============================================================================
-- V2__Add_Survey_Form_Builder.sql
-- HỆ THỐNG QUẢN LÝ CHƯƠNG TRÌNH ĐÀO TẠO & ĐỀ CƯƠNG CHI TIẾT (EUNI CLM)
-- MÔ-ĐUN BƯỚC 1: BIỂU MẪU KHẢO SÁT NHU CẦU ĐÀO TẠO & THU THẬP KẾT QUẢ PUBLIC
-- ============================================================================

-- 1. Sequences cho các bảng mới
CREATE SEQUENCE IF NOT EXISTS survey_forms_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS survey_sections_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS survey_questions_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS survey_question_options_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS survey_submissions_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS survey_answers_id_seq START WITH 1 INCREMENT BY 1;

-- 2. Bảng Biểu mẫu khảo sát (Survey Forms)
CREATE TABLE survey_forms (
    id BIGINT DEFAULT nextval('survey_forms_id_seq') PRIMARY KEY,
    campaign_step_id BIGINT REFERENCES survey_campaign_steps(id) ON DELETE SET NULL,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    target_audience VARCHAR(255),
    public_slug VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, PUBLISHED, CLOSED
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_survey_forms_step ON survey_forms(campaign_step_id);
CREATE INDEX idx_survey_forms_program ON survey_forms(program_id);
CREATE INDEX idx_survey_forms_slug ON survey_forms(public_slug);
CREATE INDEX idx_survey_forms_status ON survey_forms(status);
CREATE INDEX idx_survey_forms_deleted ON survey_forms(is_deleted);

-- 3. Bảng Phần/Chương trong biểu mẫu (Survey Sections)
CREATE TABLE survey_sections (
    id BIGINT DEFAULT nextval('survey_sections_id_seq') PRIMARY KEY,
    form_id BIGINT NOT NULL REFERENCES survey_forms(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_survey_sections_form ON survey_sections(form_id);
CREATE INDEX idx_survey_sections_deleted ON survey_sections(is_deleted);

-- 4. Bảng Câu hỏi khảo sát (Survey Questions)
CREATE TABLE survey_questions (
    id BIGINT DEFAULT nextval('survey_questions_id_seq') PRIMARY KEY,
    form_id BIGINT NOT NULL REFERENCES survey_forms(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES survey_sections(id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    help_text TEXT,
    question_type VARCHAR(30) NOT NULL, -- SHORT_TEXT, LONG_TEXT, SINGLE_CHOICE, MULTIPLE_CHOICE, LIKERT_SCALE
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    order_index INT NOT NULL DEFAULT 0,
    min_rating INT DEFAULT 1,
    max_rating INT DEFAULT 5,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_survey_questions_form ON survey_questions(form_id);
CREATE INDEX idx_survey_questions_section ON survey_questions(section_id);
CREATE INDEX idx_survey_questions_deleted ON survey_questions(is_deleted);

-- 5. Bảng Lựa chọn cho câu hỏi trắc nghiệm / Likert (Survey Question Options)
CREATE TABLE survey_question_options (
    id BIGINT DEFAULT nextval('survey_question_options_id_seq') PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES survey_questions(id) ON DELETE CASCADE,
    content VARCHAR(500) NOT NULL,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_survey_options_question ON survey_question_options(question_id);
CREATE INDEX idx_survey_options_deleted ON survey_question_options(is_deleted);

-- 6. Bảng Phiên nộp bài khảo sát (Survey Submissions)
CREATE TABLE survey_submissions (
    id BIGINT DEFAULT nextval('survey_submissions_id_seq') PRIMARY KEY,
    form_id BIGINT NOT NULL REFERENCES survey_forms(id) ON DELETE CASCADE,
    respondent_name VARCHAR(255),
    respondent_email VARCHAR(255),
    respondent_phone VARCHAR(50),
    organization VARCHAR(255),
    position VARCHAR(255),
    submitted_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_survey_submissions_form ON survey_submissions(form_id);
CREATE INDEX idx_survey_submissions_submitted ON survey_submissions(submitted_at);
CREATE INDEX idx_survey_submissions_deleted ON survey_submissions(is_deleted);

-- 7. Bảng Chi tiết câu trả lời (Survey Answers)
CREATE TABLE survey_answers (
    id BIGINT DEFAULT nextval('survey_answers_id_seq') PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES survey_submissions(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES survey_questions(id) ON DELETE CASCADE,
    text_value TEXT,
    numeric_value INT,
    selected_options TEXT, -- JSON string array lưu các option_id hoặc nội dung lựa chọn
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_survey_answers_submission ON survey_answers(submission_id);
CREATE INDEX idx_survey_answers_question ON survey_answers(question_id);
CREATE INDEX idx_survey_answers_deleted ON survey_answers(is_deleted);
