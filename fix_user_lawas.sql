-- ============================================
-- FIX untuk user lawas di database
-- Jalankan di MySQL console / phpMyAdmin
-- ============================================

-- 1. Update is_active = true untuk user lawas yang kolomnya NULL
UPDATE users SET is_active = true WHERE is_active IS NULL;

-- 2. Reset password admin/petugas ke "123456" (BCrypt hash)
-- Hash ini cocok untuk password "123456"
UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE role IN ('ADMIN', 'PETUGAS');

-- 3. Verifikasi hasil
SELECT id, nama, username, role, is_active,
       LEFT(password, 20) AS password_prefix
FROM users;
