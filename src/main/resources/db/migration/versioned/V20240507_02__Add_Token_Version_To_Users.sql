-- Add token_version to users table
ALTER TABLE users ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;
