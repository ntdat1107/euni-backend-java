-- V20240520_01: Create Survey Campaign Courses Table
-- Purpose: Support detailed course syllabus and CLO specifications per campaign course (Step 5).

DROP TABLE IF EXISTS survey_campaign_courses CASCADE;
DROP SEQUENCE IF EXISTS survey_campaign_courses_id_seq CASCADE;

CREATE SEQUENCE survey_campaign_courses_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS survey_campaign_courses (
    id BIGINT DEFAULT nextval('survey_campaign_courses_id_seq') PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES survey_campaigns(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    syllabus_data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_survey_campaign_course UNIQUE(campaign_id, course_id)
);

CREATE INDEX idx_survey_campaign_courses_campaign ON survey_campaign_courses(campaign_id);
CREATE INDEX idx_survey_campaign_courses_course ON survey_campaign_courses(course_id);
CREATE INDEX idx_survey_campaign_courses_deleted ON survey_campaign_courses(is_deleted);
