-- =====================================================================
--  V13 — add admin moderation to scripts (บทละคร / งานวิจัย)
--
--  Mirrors the job-post workflow: a script is PENDING until an admin
--  APPROVES it; only APPROVED scripts appear in the public Play Library.
--  An admin may REJECT with a note shown to the owner. Editing a script
--  sends it back to PENDING (handled in the service layer).
--
--  ddl-auto=validate requires these columns to match the Script entity.
--  Idempotent so it is safe whether the column already existed or not.
-- =====================================================================

-- 1) status column with a DB-level default so existing rows stay valid
ALTER TABLE scripts
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- 2) admin reject note (shown to the owner)
ALTER TABLE scripts
    ADD COLUMN IF NOT EXISTS review_note VARCHAR(500);

-- 3) Backfill: every script that already exists was publicly visible before
--    this change, so keep it visible by marking it APPROVED. Only brand-new
--    rows created after this migration start as PENDING.
UPDATE scripts SET status = 'APPROVED'
 WHERE status IS NULL OR status = 'PENDING';

-- 4) normalize any unexpected values into the known set
UPDATE scripts
   SET status = 'PENDING'
 WHERE status NOT IN ('PENDING', 'APPROVED', 'REJECTED');
