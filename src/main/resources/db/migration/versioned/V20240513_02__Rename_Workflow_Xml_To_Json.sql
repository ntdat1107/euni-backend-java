-- V20240513_02__Rename_Workflow_Xml_To_Json.sql
ALTER TABLE workflow_templates RENAME COLUMN xml_content TO json_content;
ALTER TABLE workflow_template_drafts RENAME COLUMN xml_content TO json_content;
