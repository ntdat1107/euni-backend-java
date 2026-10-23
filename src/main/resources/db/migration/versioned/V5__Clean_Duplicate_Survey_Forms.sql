-- ============================================================================
-- V5__Clean_Duplicate_Survey_Forms.sql
-- Làm sạch dữ liệu form rác sinh tự động trước đây, đưa step về PENDING
-- và bổ sung Partial Unique Index bảo vệ is_current
-- ============================================================================

-- 1. Xóa các form tự động sinh ra trước đây mà chưa có bất kỳ phản hồi nào (submissions = 0)
-- để trả step về trạng thái chưa khởi tạo form (chưa có version nào)
DELETE FROM survey_question_options 
WHERE question_id IN (
    SELECT id FROM survey_questions 
    WHERE form_id IN (
        SELECT id FROM survey_forms 
        WHERE id NOT IN (SELECT DISTINCT form_id FROM survey_submissions WHERE is_deleted = FALSE)
    )
);

DELETE FROM survey_questions 
WHERE form_id IN (
    SELECT id FROM survey_forms 
    WHERE id NOT IN (SELECT DISTINCT form_id FROM survey_submissions WHERE is_deleted = FALSE)
);

DELETE FROM survey_sections 
WHERE form_id IN (
    SELECT id FROM survey_forms 
    WHERE id NOT IN (SELECT DISTINCT form_id FROM survey_submissions WHERE is_deleted = FALSE)
);

DELETE FROM survey_forms 
WHERE id NOT IN (SELECT DISTINCT form_id FROM survey_submissions WHERE is_deleted = FALSE);

-- 2. Đưa các campaign_step mà chưa có form nào thực thụ về trạng thái PENDING
UPDATE survey_campaign_steps 
SET status = 'PENDING',
    result_data = NULL
WHERE id NOT IN (SELECT DISTINCT campaign_step_id FROM survey_forms WHERE is_deleted = FALSE)
  AND status = 'IN_PROGRESS';

-- 3. Đảm bảo mỗi campaign_step_id chỉ có tối đa duy nhất 1 bản ghi có is_current = TRUE
WITH current_ranks AS (
    SELECT id, campaign_step_id,
           ROW_NUMBER() OVER (
               PARTITION BY campaign_step_id
               ORDER BY version DESC, id DESC
           ) as rn
    FROM survey_forms
    WHERE is_current = TRUE AND is_deleted = FALSE
)
UPDATE survey_forms
SET is_current = FALSE
WHERE id IN (
    SELECT id FROM current_ranks WHERE rn > 1
);

-- 4. Thêm partial unique index cấp Database để ngăn chặn race condition tạo nhiều hơn 1 form active
CREATE UNIQUE INDEX IF NOT EXISTS uq_survey_forms_step_current 
ON survey_forms (campaign_step_id) 
WHERE is_current = TRUE AND is_deleted = FALSE;
