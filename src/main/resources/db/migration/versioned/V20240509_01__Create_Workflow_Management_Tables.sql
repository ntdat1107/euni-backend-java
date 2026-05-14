-- V20240509_01__Create_Workflow_Management_Tables.sql

DROP TABLE IF EXISTS workflow_template_drafts CASCADE;
DROP TABLE IF EXISTS workflow_templates CASCADE;

CREATE TABLE workflow_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    json_content TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE workflow_template_drafts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID,
    name VARCHAR(255),
    description TEXT,
    code VARCHAR(100),
    json_content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_saved_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT fk_draft_template FOREIGN KEY (template_id) REFERENCES workflow_templates(id) ON DELETE CASCADE
);

CREATE INDEX idx_workflow_templates_code ON workflow_templates(code);
CREATE INDEX idx_workflow_template_drafts_template_id ON workflow_template_drafts(template_id);
