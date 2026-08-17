-- Migration: Create workflow step definitions table and rename campaign steps documents column
-- Created: 2026-05-15

DROP TABLE IF EXISTS workflow_step_definitions CASCADE;
DROP SEQUENCE IF EXISTS workflow_step_definitions_id_seq CASCADE;

CREATE SEQUENCE workflow_step_definitions_id_seq START WITH 1 INCREMENT BY 1;

-- Create workflow step definitions table
CREATE TABLE IF NOT EXISTS workflow_step_definitions (
    id BIGINT DEFAULT nextval('workflow_step_definitions_id_seq') PRIMARY KEY,
    workflow_type VARCHAR(255) NOT NULL,
    step_code VARCHAR(255) NOT NULL UNIQUE,
    step_name VARCHAR(255) NOT NULL,
    type VARCHAR(255),
    required_documents TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE
);