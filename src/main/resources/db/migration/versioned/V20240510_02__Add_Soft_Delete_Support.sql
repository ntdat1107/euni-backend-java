-- V20240510_03: Add Soft Delete Support
-- Author: Antigravity (AI Engineering Assistant)
-- Purpose: Add is_deleted column to all major tables for soft delete functionality.

ALTER TABLE users ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE roles ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE permissions ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE faculties ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE majors ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE programs ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE courses ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE program_courses ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE workflow_templates ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE workflow_template_drafts ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Optional: Add index for performance on soft-deleted filters
CREATE INDEX idx_users_deleted ON users(is_deleted);
CREATE INDEX idx_programs_deleted ON programs(is_deleted);
CREATE INDEX idx_courses_deleted ON courses(is_deleted);
CREATE INDEX idx_program_courses_deleted ON program_courses(is_deleted);
