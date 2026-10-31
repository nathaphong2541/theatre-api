-- =====================================================================
--  V15 — update the seeded admin's email
--
--  V12 seeded the admin as admin@thaitheatre.th. Change it to the real
--  address: thaiarthub@gmail.com. The password is unchanged (Admin@1234).
--
--  Idempotent & safe:
--   1) If the old admin row exists AND the new email is not already taken,
--      rename it to the new email.
--   2) Make sure whichever row holds the new email is a usable ADMIN.
-- =====================================================================

-- 1) Rename the old seeded admin to the new email (only if the new email is free).
UPDATE users
   SET email      = 'thaiarthub@gmail.com',
       updated_at = now()
 WHERE lower(email) = 'admin@thaitheatre.th'
   AND NOT EXISTS (
       SELECT 1 FROM users u2 WHERE lower(u2.email) = 'thaiarthub@gmail.com'
   );

-- 2) Ensure the new-email account is an active ADMIN (keeps the known password).
UPDATE users
   SET role          = 'ADMIN',
       record_status = 'A',
       del_flag      = 'N',
       updated_at    = now()
 WHERE lower(email) = 'thaiarthub@gmail.com';
