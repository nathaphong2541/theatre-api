-- =====================================================================
--  V11 — scheduling dates for job posts
--
--  start_date: when the post begins showing publicly (null = immediately).
--  end_date:   last public day; after it the post is treated as expired and
--              drops out of the public search (null = never expires).
--
--  A separate migration (not editing V10) because V10 may already be applied;
--  Flyway rejects changes to an applied migration. ddl-auto=validate also needs
--  these columns to match the JobPost entity.
-- =====================================================================

ALTER TABLE job_posts ADD COLUMN IF NOT EXISTS start_date date;
ALTER TABLE job_posts ADD COLUMN IF NOT EXISTS end_date date;
