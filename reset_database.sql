-- ==============================================================================
-- EUNI CLEANUP SCRIPT: RESET ENTIRE DATABASE SCHEMA FOR FRESH FLYWAY MIGRATION
-- Database: PostgreSQL (euni_db)
-- ==============================================================================

-- 1. Xóa toàn bộ schema public hiện tại cùng mọi bảng, index, sequence, trigger, views và flyway_schema_history
DROP SCHEMA IF EXISTS public CASCADE;

-- 2. Tái tạo lại schema public hoàn toàn mới
CREATE SCHEMA public;

-- 3. Cấp lại toàn quyền cho user hiện tại và user public
GRANT ALL ON SCHEMA public TO CURRENT_USER;
GRANT ALL ON SCHEMA public TO public;

-- ==============================================================================
-- Sau khi chạy xong script này trong DBeaver / pgAdmin / psql:
-- Khởi động lại ứng dụng Spring Boot Backend, Flyway sẽ tự động chạy toàn bộ
-- migration từ đầu (từ V1 -> V2024... -> V2026...) và nạp Seeder mới.
-- ==============================================================================
