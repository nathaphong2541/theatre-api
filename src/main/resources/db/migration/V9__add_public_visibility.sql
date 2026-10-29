-- =====================================================================
--  V9 — per-section public visibility toggles on a profile
--
--  A JSONB map, e.g. {"about": true, "email": false}, where a key = true means
--  that section is shown on the public profile / public API. Missing or false
--  means hidden. Default '{}' = everything private until the member opts in,
--  which matches the requested "default = not public" behaviour.
--
--  The login email (users.email) is never represented here and is never public.
--  Added because spring.jpa.hibernate.ddl-auto=validate requires every column
--  to exist in a migration.
-- =====================================================================

ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS public_visibility jsonb NOT NULL DEFAULT '{}'::jsonb;
