-- =====================================================================
--  V8 — add "Lyricist / นักเขียนเนื้อเพลง" to the professions master list
--
--  The site is open to writers who don't perform (playwrights, book writers,
--  lyricists), so we add Lyricist as profession id 21 (next after the 20 roles
--  seeded in V3). Profiles reference professions by integer id in the JSONB
--  `profiles.title` array, so the new id is immediately selectable/searchable.
--
--  del_flag/record_status are set explicitly ('N'/'A') because the public
--  endpoint filters on them and raw INSERTs don't get the entity defaults.
--
--  Idempotent: safe to re-run (ON CONFLICT on the primary key).
-- =====================================================================

INSERT INTO professions (id, name_th, name_en, description, del_flag, record_status, created_at, updated_at)
VALUES (21, 'นักเขียนเนื้อเพลง', 'Lyricist', NULL, 'N', 'A', now(), now())
ON CONFLICT (id) DO UPDATE
    SET name_th = EXCLUDED.name_th,
        name_en = EXCLUDED.name_en,
        del_flag = EXCLUDED.del_flag,
        record_status = EXCLUDED.record_status;

-- Keep the identity sequence ahead of the highest id so future admin inserts
-- don't collide.
SELECT setval('professions_id_seq', GREATEST((SELECT MAX(id) FROM professions), 21));
