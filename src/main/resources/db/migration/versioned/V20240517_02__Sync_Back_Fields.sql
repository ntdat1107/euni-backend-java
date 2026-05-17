-- V20240517_02: Sync-Back Fields for Master Data
-- Purpose: Add generic data fields to store information synced back from survey campaigns.

ALTER TABLE program_courses ADD COLUMN data TEXT; -- Stores synced data (including CLOs) from Step 5
ALTER TABLE programs ADD COLUMN data TEXT; -- Stores synced data (including PEOs/PLOs/PIs) from Step 2
