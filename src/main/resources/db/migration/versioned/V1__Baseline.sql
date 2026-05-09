-- V1: Baseline Schema
-- Author: Antigravity
-- Purpose: Initial empty schema for versioning.

CREATE TABLE IF NOT EXISTS schema_version_info (
    id SERIAL PRIMARY KEY,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO schema_version_info (description) VALUES ('Initial schema setup');
