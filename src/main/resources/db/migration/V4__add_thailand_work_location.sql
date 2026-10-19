-- =====================================================================
--  V4 — add "Thailand / ประเทศไทย" as a work-location option
--
--  The work_location master list previously held only US/Canada regions
--  (plus an "Other" row). This adds Thailand as id 18.
--
--  del_flag/record_status are set explicitly ('N'/'A'): they default only
--  in the JPA entity, not at the DB level, and the public endpoint filters
--  on del_flag='N' AND record_status='A' (a NULL row would be hidden).
--
--  Idempotent: safe to re-run (ON CONFLICT on the primary key).
-- =====================================================================

INSERT INTO work_location (id, name_th, name_en, del_flag, record_status)
VALUES (18, 'ประเทศไทย', 'Thailand', 'N', 'A')
ON CONFLICT (id) DO UPDATE
    SET name_th = EXCLUDED.name_th,
        name_en = EXCLUDED.name_en,
        del_flag = EXCLUDED.del_flag,
        record_status = EXCLUDED.record_status;

-- Keep the identity sequence ahead of the highest id so future inserts
-- (e.g. via the admin screen) don't collide.
SELECT setval('work_location_id_seq', GREATEST((SELECT MAX(id) FROM work_location), 18));
