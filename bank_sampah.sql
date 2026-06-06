-- =============================================================
-- Bank Sampah Migration
-- Menambahkan fitur Bank Sampah: saldo user, daur ulang kategori,
-- transaksi saldo, dan penarikan saldo.
-- =============================================================

-- 1. Tambah kolom saldo ke tabel users
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS saldo DECIMAL(15,2) DEFAULT 0;

-- 2. Tambah kolom daur ulang ke tabel kategori_sampah
ALTER TABLE kategori_sampah
  ADD COLUMN IF NOT EXISTS is_daur_ulang BOOLEAN DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS harga_daur_ulang DECIMAL(10,2) NULL;

-- 3. Tabel riwayat transaksi saldo
CREATE TABLE IF NOT EXISTS saldo_transaksi (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  tipe ENUM('KREDIT','DEBIT') NOT NULL,
  jumlah DECIMAL(15,2) NOT NULL,
  saldo_sebelum DECIMAL(15,2) NOT NULL,
  saldo_sesudah DECIMAL(15,2) NOT NULL,
  keterangan VARCHAR(255),
  setoran_id BIGINT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (setoran_id) REFERENCES setoran(id)
);

-- 4. Tabel permintaan penarikan saldo
CREATE TABLE IF NOT EXISTS penarikan_saldo (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  warga_id BIGINT NOT NULL,
  petugas_id BIGINT NULL,
  jumlah DECIMAL(15,2) NOT NULL,
  status ENUM('MENUNGGU','DISETUJUI','DITOLAK') DEFAULT 'MENUNGGU',
  catatan VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (warga_id) REFERENCES users(id),
  FOREIGN KEY (petugas_id) REFERENCES users(id)
);
