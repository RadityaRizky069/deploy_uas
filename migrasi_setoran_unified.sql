-- ============================================
-- MIGRASI: Gabung setoran_sampah & setoran_uang ke tabel setoran (unified)
-- Jalankan di MySQL console / phpMyAdmin
-- ============================================

-- 1. Backup tabel lama
RENAME TABLE setoran TO setoran_backup;

-- 2. Buat tabel setoran baru (unified)
CREATE TABLE `setoran` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `jenis_setoran` enum('SAMPAH','UANG') NOT NULL,
  `warga_id` bigint(20) NOT NULL,
  `petugas_id` bigint(20) DEFAULT NULL,
  `kategori_id` bigint(20) DEFAULT NULL,
  `berat_kg` decimal(10,2) DEFAULT NULL,
  `total_harga` decimal(12,2) DEFAULT NULL,
  `status` enum('MENUNGGU','DITERIMA','DITOLAK') DEFAULT NULL,
  `alamat_jemput` varchar(255) DEFAULT NULL,
  `bukti_pembayaran` varchar(255) DEFAULT NULL,
  `status_pembayaran` enum('MENUNGGU_VERIFIKASI','DISETUJUI','DITOLAK') DEFAULT NULL,
  `status_penjemputan` enum('DIJADWALKAN','SEDANG_DIJEMPUT','SELESAI') DEFAULT NULL,
  `jumlah_uang` decimal(15,2) DEFAULT NULL,
  `jenis_uang` varchar(255) DEFAULT NULL,
  `deskripsi` varchar(255) DEFAULT NULL,
  `catatan` varchar(255) DEFAULT NULL,
  `jenis_sampah` enum('ORGANIK','ANORGANIK','B3') DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_warga` (`warga_id`),
  KEY `fk_petugas` (`petugas_id`),
  KEY `fk_kategori` (`kategori_id`),
  CONSTRAINT `fk_setoran_warga` FOREIGN KEY (`warga_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_setoran_petugas` FOREIGN KEY (`petugas_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_setoran_kategori` FOREIGN KEY (`kategori_id`) REFERENCES `kategori_sampah` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 3. Migrasi data dari setoran_backup (data lama) dengan default status
INSERT INTO setoran (id, jenis_setoran, warga_id, berat_kg, jumlah_uang, jenis_sampah, catatan, status, created_at)
SELECT id, jenis_setoran, user_id, berat_kg, jumlah_uang, jenis_sampah, keterangan,
       CASE WHEN jenis_setoran = 'SAMPAH' THEN 'MENUNGGU' ELSE NULL END,
       tanggal
FROM setoran_backup;

-- 4. Migrasi data dari setoran_sampah (jika ada)
INSERT INTO setoran (jenis_setoran, warga_id, petugas_id, kategori_id, berat_kg, total_harga, status, alamat_jemput, bukti_pembayaran, status_pembayaran, status_penjemputan, catatan, created_at, updated_at)
SELECT 'SAMPAH', warga_id, petugas_id, kategori_id, berat_kg, total_harga, status, alamat_jemput, bukti_pembayaran, status_pembayaran, status_penjemputan, catatan, created_at, updated_at
FROM setoran_sampah;

-- 5. Migrasi data dari setoran_uang (jika ada)
INSERT INTO setoran (jenis_setoran, warga_id, petugas_id, jumlah_uang, jenis_uang, deskripsi, created_at)
SELECT 'UANG', warga_id, petugas_id, jumlah, jenis, deskripsi, created_at
FROM setoran_uang;

-- 6. Hapus tabel lama
DROP TABLE IF EXISTS setoran_backup;
DROP TABLE IF EXISTS setoran_sampah;
DROP TABLE IF EXISTS setoran_uang;

-- 7. Verifikasi hasil
SELECT 'SETORAN' AS tabel, COUNT(*) AS jumlah FROM setoran
UNION ALL
SELECT 'USERS', COUNT(*) FROM users;
