-- V20240520_01: Create Survey Campaign Courses Table
-- Purpose: Support detailed course syllabus and CLO specifications per campaign course (Step 5).

CREATE TABLE IF NOT EXISTS survey_campaign_courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES survey_campaigns(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id),
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
