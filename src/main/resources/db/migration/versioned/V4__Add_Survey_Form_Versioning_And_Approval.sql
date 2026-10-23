-- ============================================================================
-- V4__Add_Survey_Form_Versioning_And_Approval.sql
-- Thêm versioning, cờ is_current, previous_version_id cho survey_forms
-- Hỗ trợ quy trình phê duyệt thiết kế biểu mẫu và xem lịch sử các phiên bản
-- ============================================================================

ALTER TABLE survey_forms ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 1;
ALTER TABLE survey_forms ADD COLUMN IF NOT EXISTS is_current BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE survey_forms ADD COLUMN IF NOT EXISTS previous_version_id BIGINT REFERENCES survey_forms(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_survey_forms_step_current ON survey_forms(campaign_step_id, is_current);
CREATE INDEX IF NOT EXISTS idx_survey_forms_step_version ON survey_forms(campaign_step_id, version);
