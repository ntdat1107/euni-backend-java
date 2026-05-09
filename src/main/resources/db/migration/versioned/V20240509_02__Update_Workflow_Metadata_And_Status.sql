-- V20240509_02__Update_Workflow_Metadata_And_Status.sql
-- Thêm description và status vào bản nháp
ALTER TABLE workflow_template_drafts ADD COLUMN description TEXT;
ALTER TABLE workflow_template_drafts ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';

-- Thêm status vào bản chính thức (đã có description từ migration trước)
ALTER TABLE workflow_templates ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
