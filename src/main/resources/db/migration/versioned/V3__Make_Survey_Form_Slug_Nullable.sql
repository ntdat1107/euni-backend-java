-- ============================================================================
-- V3__Make_Survey_Form_Slug_Nullable.sql
-- Cho phép public_slug null khi ở trạng thái DRAFT (chỉ sinh slug khi Xuất bản)
-- ============================================================================

ALTER TABLE survey_forms ALTER COLUMN public_slug DROP NOT NULL;
