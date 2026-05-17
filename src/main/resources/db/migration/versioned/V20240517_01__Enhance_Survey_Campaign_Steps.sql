-- V20240517_01: Enhance Survey Campaign Steps
-- Purpose: Add status and result_data to survey_campaign_steps for better tracking and data storage.

ALTER TABLE survey_campaign_steps ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE survey_campaign_steps ADD COLUMN result_data TEXT; -- Actual survey output for the step (JSON)
