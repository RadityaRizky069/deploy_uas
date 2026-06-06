package com.example.trashformer.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.trashformer.model.JenisSetoran;
import com.example.trashformer.model.Setoran;
import com.example.trashformer.model.StatusPembayaran;
import com.example.trashformer.model.StatusPenjemputan;
import com.example.trashformer.model.StatusSetoran;

public interface SetoranRepository extends JpaRepository<Setoran, Long> {

    List<Setoran> findByWargaIdOrderByCreatedAtDesc(Long wargaId);

    List<Setoran> findByJenisSetoranOrderByCreatedAtDesc(JenisSetoran jenisSetoran);

    List<Setoran> findByJenisSetoranAndWargaIdOrderByCreatedAtDesc(JenisSetoran jenisSetoran, Long wargaId);

    List<Setoran> findByStatusOrderByCreatedAtDesc(StatusSetoran status);

    List<Setoran> findByPetugasIdOrderByCreatedAtDesc(Long petugasId);

    List<Setoran> findByStatusPembayaranOrderByCreatedAtDesc(StatusPembayaran statusPembayaran);

    long countByStatus(StatusSetoran status);

    long countByStatusPembayaran(StatusPembayaran statusPembayaran);

    long countByStatusPenjemputan(StatusPenjemputan statusPenjemputan);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.beratKg), 0) FROM Setoran s WHERE s.kategori.id = :kategoriId AND s.status = 'DITERIMA'")
    Double sumBeratByKategoriId(@Param("kategoriId") Long kategoriId);

    @Query("SELECT COALESCE(SUM(s.beratKg), 0) FROM Setoran s WHERE s.createdAt BETWEEN :start AND :end")
    Double sumBeratByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.beratKg), 0) FROM Setoran s WHERE s.status = 'DITERIMA'")
    Double sumAllBeratDiterima();

    @Query("SELECT k.nama, COALESCE(SUM(s.beratKg), 0) FROM Setoran s JOIN s.kategori k WHERE s.status = 'DITERIMA' GROUP BY k.nama")
    List<Object[]> sumBeratPerKategori();

    @Query("SELECT MONTH(s.createdAt), COALESCE(SUM(s.beratKg), 0) FROM Setoran s WHERE YEAR(s.createdAt) = :year AND s.jenisSetoran = 'SAMPAH' GROUP BY MONTH(s.createdAt) ORDER BY MONTH(s.createdAt)")
    List<Object[]> sumBeratPerBulan(@Param("year") int year);

    @Query("SELECT MONTH(s.createdAt), COALESCE(SUM(s.beratKg), 0) FROM Setoran s WHERE s.warga.id = :wargaId AND YEAR(s.createdAt) = :year AND s.jenisSetoran = 'SAMPAH' GROUP BY MONTH(s.createdAt) ORDER BY MONTH(s.createdAt)")
    List<Object[]> sumBeratPerBulanByWarga(@Param("wargaId") Long wargaId, @Param("year") int year);

    @Query("SELECT COALESCE(SUM(s.jumlahUang), 0) FROM Setoran s WHERE s.jenisSetoran = 'UANG' AND s.jenisUang = :jenis")
    Double sumJumlahUangByJenis(@Param("jenis") String jenis);
}
