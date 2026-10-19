-- =====================================================================
--  V6 — add `copyright_owner` to scripts
--
--  Holds the copyright holder's name shown on the script detail page,
--  e.g. "© 2026 Author's Name. All rights reserved.".
--  Nullable so existing rows and uploads without an explicit owner stay
--  valid; the UI falls back to the uploader's name when it is blank.
--
--  Idempotent so it is safe whether the column was already created by a
--  previous Hibernate ddl-auto run or not.
-- =====================================================================

ALTER TABLE scripts
    ADD COLUMN IF NOT EXISTS copyright_owner VARCHAR(255);
