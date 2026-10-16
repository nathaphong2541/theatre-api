-- =====================================================================
--  V2 — add `category` to scripts
--
--  Values: 'SCRIPT'   = บทละคร (default)
--          'RESEARCH' = เรื่องเล่าและงานวิจัย
--
--  Idempotent so it is safe whether the column was already created by the
--  previous Hibernate ddl-auto run or not.
-- =====================================================================

-- 1) add the column with a DB-level default so existing rows stay valid
ALTER TABLE scripts
    ADD COLUMN IF NOT EXISTS category VARCHAR(32) NOT NULL DEFAULT 'SCRIPT';

-- 2) backfill any pre-existing NULLs (if the column existed nullable before)
UPDATE scripts SET category = 'SCRIPT' WHERE category IS NULL;

-- 3) normalize unexpected values into the known set
UPDATE scripts
   SET category = 'SCRIPT'
 WHERE category NOT IN ('SCRIPT', 'RESEARCH');
