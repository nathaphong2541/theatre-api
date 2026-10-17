-- =====================================================================
--  V3 — replace the professions master list with the 20 Home-dropdown roles
--
--  The old seed had 16 rows whose ids/meanings don't line up with the new
--  ordered list. We renumber to 1..20 to match the Home quick-search order
--  exactly, and remap every profile's stored `profiles.title` JSONB id-array
--  from the old ids to the new ones so existing members keep the right role.
--
--  NOTE: professions are referenced by integer id (not a FK) from the JSONB
--  arrays in profiles.title. There is no DB-level referential integrity, so
--  this migration is the only thing keeping existing profiles consistent.
--
--  "Other / อื่น ๆ"  -> client sentinel id 999 (never a real row here).
--  "All Roles"        -> the empty / no-filter default (never a real row).
--
--  Idempotent: safe to re-run.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1) Remap existing profile.title ids (old -> new) BEFORE replacing rows.
--
--    Old -> New (only ids whose meaning still exists in the new list):
--      1  Acting             -> 1  Actor
--      2  Singer             -> 4  Singer
--      3  Dancer             -> 2  Dancer
--      5  Assistant Director -> 6  Director / Assistant Director
--      6  Actor Direction    -> 6  Director / Assistant Director
--      8  Dramaturgy         -> 8  Dramaturg
--
--    Old ids with no equivalent in the new list (4 Movement, 7 Rehearsal
--    Direction, 9 Staging, 10 Film, 11 Series, 12 Commercials, 13 Theatre,
--    14 Musical, 15 Short Film, 16 Documentary) are mapped to the sentinel
--    999 ("Other") so the data stays valid and visible rather than pointing
--    at a role that changed meaning. Duplicates (e.g. old 5 and 6 both -> 6)
--    are de-duplicated.
--
--    We use a temporary offset (+1000) so the remap can't collide with ids
--    that are both a source and a target (e.g. 2->4 while 3->2).
-- ---------------------------------------------------------------------

-- 1a) shift every title id into a temporary high range (old + 1000)
UPDATE profiles
   SET title = (
       SELECT COALESCE(jsonb_agg(DISTINCT (elem::int + 1000)), '[]'::jsonb)
       FROM jsonb_array_elements_text(title) AS elem
   )
 WHERE jsonb_typeof(title) = 'array'
   AND jsonb_array_length(title) > 0;

-- 1b) map the shifted ids down to their new values (de-duplicated per profile)
UPDATE profiles
   SET title = (
       SELECT COALESCE(jsonb_agg(DISTINCT new_id), '[]'::jsonb)
       FROM (
           SELECT CASE (elem::int - 1000)
                      WHEN 1  THEN 1    -- Acting             -> Actor
                      WHEN 2  THEN 4    -- Singer             -> Singer
                      WHEN 3  THEN 2    -- Dancer             -> Dancer
                      WHEN 5  THEN 6    -- Assistant Director -> Director/Asst
                      WHEN 6  THEN 6    -- Actor Direction    -> Director/Asst
                      WHEN 8  THEN 8    -- Dramaturgy         -> Dramaturg
                      WHEN 999 THEN 999 -- keep existing "Other" sentinel
                      ELSE 999          -- no equivalent -> Other
                  END AS new_id
           FROM jsonb_array_elements_text(title) AS elem
       ) mapped
   )
 WHERE jsonb_typeof(title) = 'array'
   AND jsonb_array_length(title) > 0;

-- ---------------------------------------------------------------------
-- 2) Replace the professions rows with the new ordered 20-role list.
-- ---------------------------------------------------------------------
DELETE FROM professions;

-- IMPORTANT: del_flag/record_status default to 'N'/'A' only in the JPA entity
-- (BaseEntity), NOT at the DB level. A raw SQL INSERT therefore leaves them
-- NULL, and the public endpoint filters WHERE del_flag='N' AND record_status='A'
-- — so NULL rows are invisible and the dropdown comes back empty. Always set
-- them explicitly here. created_at/updated_at are set too for completeness.
INSERT INTO professions (id, name_th, name_en, description, del_flag, record_status, created_at, updated_at) VALUES
(1,  'นักแสดง', 'Actor', NULL, 'N', 'A', now(), now()),
(2,  'นักเต้น', 'Dancer', NULL, 'N', 'A', now(), now()),
(3,  'ผู้ออกแบบท่าเต้น', 'Choreographer', NULL, 'N', 'A', now(), now()),
(4,  'นักร้อง', 'Singer', NULL, 'N', 'A', now(), now()),
(5,  'นักดนตรี', 'Musician', NULL, 'N', 'A', now(), now()),
(6,  'ผู้กำกับการแสดง / ผู้ช่วยผู้กำกับ', 'Director / Assistant Director', NULL, 'N', 'A', now(), now()),
(7,  'นักเขียนบท', 'Scriptwriter / Playwright', NULL, 'N', 'A', now(), now()),
(8,  'ผู้ช่วยวิเคราะห์และพัฒนางานละคร', 'Dramaturg', NULL, 'N', 'A', now(), now()),
(9,  'นักประพันธ์เพลง', 'Composer', NULL, 'N', 'A', now(), now()),
(10, 'ผู้ออกแบบเสียง', 'Sound Designer', NULL, 'N', 'A', now(), now()),
(11, 'ผู้ออกแบบฉาก / อุปกรณ์ประกอบฉาก', 'Set / Props Designer', NULL, 'N', 'A', now(), now()),
(12, 'ผู้ออกแบบแสง', 'Lighting Designer', NULL, 'N', 'A', now(), now()),
(13, 'ผู้ออกแบบเครื่องแต่งกาย', 'Costume Designer', NULL, 'N', 'A', now(), now()),
(14, 'ช่างแต่งหน้า / ช่างทำผม', 'Makeup Artist / Hair Stylist', NULL, 'N', 'A', now(), now()),
(15, 'โปรดิวเซอร์ / ผู้จัดการการผลิต', 'Producer / Production Manager', NULL, 'N', 'A', now(), now()),
(16, 'ผู้จัดการเวที', 'Stage Manager', NULL, 'N', 'A', now(), now()),
(17, 'ทีมเทคนิคและเวที', 'Technical & Stage Crew', NULL, 'N', 'A', now(), now()),
(18, 'ประชาสัมพันธ์', 'Public Relations', NULL, 'N', 'A', now(), now()),
(19, 'ช่างภาพ / ช่างวิดีโอ', 'Photographer / Videographer', NULL, 'N', 'A', now(), now()),
(20, 'นักออกแบบกราฟิก / สื่อดิจิทัล', 'Graphic / Digital Media Designer', NULL, 'N', 'A', now(), now());

-- ---------------------------------------------------------------------
-- 3) Keep the identity sequence ahead of the highest id so future inserts
--    (e.g. via the admin screen) don't collide.
-- ---------------------------------------------------------------------
SELECT setval('professions_id_seq', 20);
