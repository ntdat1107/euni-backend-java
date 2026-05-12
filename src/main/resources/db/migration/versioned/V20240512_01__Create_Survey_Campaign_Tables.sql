-- V20240512_01: Create Survey Campaign Tables
-- Author: Antigravity (AI Engineering Assistant)
-- Purpose: Support Survey Campaign management feature with dynamic workflow steps.

DROP TABLE IF EXISTS survey_campaign_steps;
DROP TABLE IF EXISTS survey_campaigns;

CREATE TABLE survey_campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    program_id UUID NOT NULL REFERENCES programs(id),
    workflow_template_id UUID NOT NULL REFERENCES workflow_templates(id),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE survey_campaign_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES survey_campaigns(id) ON DELETE CASCADE,
    step_index INT NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    deadline TIMESTAMP,
    documents TEXT, -- JSON array of document requirements or descriptions
    configuration TEXT, -- Full step configuration from workflow template (JSON)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_survey_campaigns_deleted ON survey_campaigns(is_deleted);
CREATE INDEX idx_survey_campaign_steps_deleted ON survey_campaign_steps(is_deleted);
CREATE INDEX idx_survey_campaigns_code ON survey_campaigns(code);
