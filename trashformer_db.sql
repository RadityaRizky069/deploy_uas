-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: May 29, 2026 at 04:49 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `trashformer_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `kategori_sampah`
--

CREATE TABLE `kategori_sampah` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deskripsi` varchar(255) DEFAULT NULL,
  `harga_per_kg` decimal(10,2) DEFAULT NULL,
  `nama` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `kategori_sampah`
--

INSERT INTO `kategori_sampah` (`id`, `created_at`, `deskripsi`, `harga_per_kg`, `nama`) VALUES
(1, '2026-05-23 12:04:17.000000', NULL, 1000.00, 'B3'),
(2, '2026-05-23 12:28:45.000000', NULL, 1500.00, 'Plastik'),
(3, '2026-05-23 12:28:55.000000', NULL, 500.00, 'organik');

-- --------------------------------------------------------

--
-- Table structure for table `setoran`
--

CREATE TABLE `setoran` (
  `id` bigint(20) NOT NULL,
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
  `updated_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `setoran`
--

INSERT INTO `setoran` (`id`, `jenis_setoran`, `warga_id`, `petugas_id`, `kategori_id`, `berat_kg`, `total_harga`, `status`, `alamat_jemput`, `bukti_pembayaran`, `status_pembayaran`, `status_penjemputan`, `jumlah_uang`, `jenis_uang`, `deskripsi`, `catatan`, `jenis_sampah`, `created_at`, `updated_at`) VALUES
(1, 'SAMPAH', 3, NULL, NULL, 5.50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Setoran sampah organik', 'ORGANIK', '2026-05-18 07:24:10.000000', NULL),
(2, 'UANG', 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 50000.00, NULL, NULL, 'Iuran kebersihan', NULL, '2026-05-18 07:24:10.000000', NULL),
(3, 'SAMPAH', 4, 2, 1, 4.00, 4000.00, 'DITERIMA', NULL, NULL, 'DISETUJUI', 'SELESAI', NULL, NULL, NULL, 'sampah', NULL, '2026-05-23 12:04:57.000000', '2026-05-23 12:07:06.000000'),
(4, 'SAMPAH', 4, 2, 1, 4.00, 4000.00, 'DITERIMA', 'katapang', '601f7be3-243e-4eb9-ac50-b3abe2fbc213.jpg', 'DISETUJUI', 'SELESAI', NULL, NULL, NULL, 'haha', NULL, '2026-05-23 12:06:19.000000', '2026-05-23 12:07:09.000000'),
(6, 'SAMPAH', 4, 2, 1, 6.00, 6000.00, 'DITERIMA', 'rancaekek', 'eefbb088-0c23-4b6d-898a-83371813f4a0.jpg', 'DISETUJUI', 'SELESAI', NULL, NULL, NULL, 'yfyfy', NULL, '2026-05-23 12:27:40.000000', '2026-05-23 12:28:07.000000');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `nama` varchar(255) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','PETUGAS','WARGA') NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `alamat` varchar(255) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `no_telepon` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `nama`, `username`, `password`, `role`, `created_at`, `updated_at`, `alamat`, `is_active`, `no_telepon`) VALUES
(1, 'Administrator', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NULL, NULL, NULL, b'1', NULL),
(2, 'Petugas Sampah', 'petugas', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'PETUGAS', NULL, NULL, NULL, b'1', NULL),
(3, 'Budi Warga', 'warga', '$2a$10$C/zMmLRMy.hva282Zyg7O.GPZEY9mrzk1N8lzjITvAbOr84CEzfl2', 'WARGA', NULL, NULL, NULL, b'1', NULL),
(4, 'harpan', 'harpan', '$2a$10$wOhX0MQXSx30U5RqqHydy.r7bACY0dB5hKyWDYNaEl8OMvN9jC8Wy', 'WARGA', '2026-05-18 07:35:45.000000', '2026-05-18 07:35:45.000000', NULL, b'1', NULL),
(5, 'harpan', 'harpan12', '$2a$10$32R5UVeI2uRA.eG0KlrN9eX2KQsT7ASrwyG1.O05wW51EnNqPldvq', 'WARGA', '2026-05-18 07:54:28.000000', '2026-05-18 07:54:28.000000', NULL, b'1', NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `kategori_sampah`
--
ALTER TABLE `kategori_sampah`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK80j8qyluygllwqvnbiin6fsyu` (`nama`);

--
-- Indexes for table `setoran`
--
ALTER TABLE `setoran`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_warga` (`warga_id`),
  ADD KEY `fk_petugas` (`petugas_id`),
  ADD KEY `fk_kategori` (`kategori_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `kategori_sampah`
--
ALTER TABLE `kategori_sampah`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `setoran`
--
ALTER TABLE `setoran`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `setoran`
--
ALTER TABLE `setoran`
  ADD CONSTRAINT `fk_setoran_kategori` FOREIGN KEY (`kategori_id`) REFERENCES `kategori_sampah` (`id`),
  ADD CONSTRAINT `fk_setoran_petugas` FOREIGN KEY (`petugas_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `fk_setoran_warga` FOREIGN KEY (`warga_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
