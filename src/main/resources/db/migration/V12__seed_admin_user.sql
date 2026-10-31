-- =====================================================================
--  V12 — seed a ready-to-use ADMIN login
--
--  Creates a dedicated admin account so the moderation endpoints
--  (/api/admin/**, hasRole('ADMIN')) and the job-post approval UI are
--  usable with a known password.
--
--      email    : admin@thaitheatre.th
--      password : Admin@1234
--
--  password_hash below is a real BCrypt (strength 10) hash of the password
--  above, produced with the same BCryptPasswordEncoder(10) the backend uses
--  to verify logins, so it authenticates correctly.
--
--  Idempotent: the INSERT only runs when the account does not yet exist,
--  and a follow-up UPDATE keeps an existing row aligned to ADMIN. Uses a
--  WHERE NOT EXISTS guard (no reliance on a named unique constraint).
-- =====================================================================

INSERT INTO users (
    first_name,
    last_name,
    email,
    password_hash,
    policy_confirmed,
    role,
    record_status,
    del_flag,
    created_at,
    updated_at
)
SELECT
    'Site',
    'Admin',
    'admin@thaitheatre.th',
    '$2a$10$l3D6M7TkDTDoqlo9zirIZeKOfPqmEQAgfIjXofkuPKvuWag9MrcNa',
    true,
    'ADMIN',
    'A',
    'N',
    now(),
    now()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE lower(email) = 'admin@thaitheatre.th'
);

-- Keep the account usable and correctly privileged if it already existed.
UPDATE users
   SET password_hash = '$2a$10$l3D6M7TkDTDoqlo9zirIZeKOfPqmEQAgfIjXofkuPKvuWag9MrcNa',
       role          = 'ADMIN',
       record_status = 'A',
       del_flag      = 'N',
       updated_at    = now()
 WHERE lower(email) = 'admin@thaitheatre.th';
