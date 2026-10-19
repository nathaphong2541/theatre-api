-- =====================================================================
--  V5 — add "Any location / สถานที่ใดก็ได้" as a work-location option
--
--  Stored like any other work_location id (19) so that a profile which
--  "works anywhere" persists that choice and anyone viewing the profile
--  can see it — instead of it being an empty/implicit value.
--
--  The UI treats id 19 as an exclusive option: picking it clears every
--  other work-location, and picking a real location clears it.
--
--  del_flag/record_status are set explicitly ('N'/'A'): they default only
--  in the JPA entity, not at the DB level, and the public endpoint filters
--  on del_flag='N' AND record_status='A' (a NULL row would be hidden).
--
--  Idempotent: safe to re-run (ON CONFLICT on the primary key).
-- =====================================================================

INSERT INTO work_location (id, name_th, name_en, del_flag, record_status)
VALUES (19, 'สถานที่ใดก็ได้', 'Any location', 'N', 'A')
ON CONFLICT (id) DO UPDATE
    SET name_th = EXCLUDED.name_th,
        name_en = EXCLUDED.name_en,
        del_flag = EXCLUDED.del_flag,
        record_status = EXCLUDED.record_status;

-- Keep the identity sequence ahead of the highest id so future inserts
-- (e.g. via the admin screen) don't collide.
SELECT setval('work_location_id_seq', GREATEST((SELECT MAX(id) FROM work_location), 19));
