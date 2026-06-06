package com.example.trashformer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trashformer.model.JenisSetoran;
import com.example.trashformer.model.KategoriSampah;
import com.example.trashformer.model.Setoran;
import com.example.trashformer.model.StatusPembayaran;
import com.example.trashformer.model.StatusPenjemputan;
import com.example.trashformer.model.StatusSetoran;
import com.example.trashformer.model.User;
import com.example.trashformer.repository.KategoriSampahRepository;
import com.example.trashformer.repository.SetoranRepository;
import com.example.trashformer.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class SetoranService {

    private final SetoranRepository setoranRepository;
    private final KategoriSampahRepository kategoriSampahRepository;
    private final UserRepository userRepository;
    private final BankSampahService bankSampahService;

    @PersistenceContext
    private EntityManager entityManager;

    public SetoranService(SetoranRepository setoranRepository,
                          KategoriSampahRepository kategoriSampahRepository,
                          UserRepository userRepository,
                          BankSampahService bankSampahService) {
        this.setoranRepository = setoranRepository;
        this.kategoriSampahRepository = kategoriSampahRepository;
        this.userRepository = userRepository;
        this.bankSampahService = bankSampahService;
    }

    public Setoran createSetoranSampah(Long wargaId, Long kategoriId, BigDecimal beratKg, String catatan) {
        User warga = userRepository.findById(wargaId)
                .orElseThrow(() -> new RuntimeException("Warga tidak ditemukan"));
        KategoriSampah kategori = kategoriSampahRepository.findById(kategoriId)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        Setoran setoran = new Setoran();
        setoran.setJenisSetoran(JenisSetoran.SAMPAH);
        setoran.setWarga(warga);
        setoran.setKategori(kategori);
        setoran.setBeratKg(beratKg);

        BigDecimal hargaPerKg = kategori.getHargaPerKg();
        if (hargaPerKg != null) {
            setoran.setTotalHarga(beratKg.multiply(hargaPerKg));
        }

        if (catatan != null && !catatan.trim().isEmpty()) {
            setoran.setCatatan(catatan.trim());
        }

        return setoranRepository.save(setoran);
    }

    public Setoran createSetoranSampahWarga(Long wargaId, Long kategoriId, BigDecimal beratKg,
                                             String alamatJemput, String catatan, String buktiPembayaran) {
        User warga = userRepository.findById(wargaId)
                .orElseThrow(() -> new RuntimeException("Warga tidak ditemukan"));
        KategoriSampah kategori = kategoriSampahRepository.findById(kategoriId)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        Setoran setoran = new Setoran();
        setoran.setJenisSetoran(JenisSetoran.SAMPAH);
        setoran.setWarga(warga);
        setoran.setKategori(kategori);
        setoran.setBeratKg(beratKg);

        BigDecimal harga;
        if (Boolean.TRUE.equals(kategori.getIsDaurUlang()) && kategori.getHargaDaurUlang() != null) {
            harga = kategori.getHargaDaurUlang();
        } else {
            harga = kategori.getHargaPerKg();
        }
        if (harga != null) {
            setoran.setTotalHarga(beratKg.multiply(harga));
        }

        if (alamatJemput != null && !alamatJemput.trim().isEmpty()) {
            setoran.setAlamatJemput(alamatJemput.trim());
        }

        if (catatan != null && !catatan.trim().isEmpty()) {
            setoran.setCatatan(catatan.trim());
        }

        if (buktiPembayaran != null && !buktiPembayaran.isEmpty()) {
            setoran.setBuktiPembayaran(buktiPembayaran);
        }

        return setoranRepository.save(setoran);
    }

    @Transactional
    public Setoran terimaSetoranDaurUlang(Long setoranId, Long petugasId) {
        Setoran setoran = setoranRepository.findById(setoranId)
                .orElseThrow(() -> new RuntimeException("Setoran tidak ditemukan"));
        User petugas = userRepository.findById(petugasId)
                .orElseThrow(() -> new RuntimeException("Petugas tidak ditemukan"));

        setoran.setStatus(StatusSetoran.DITERIMA);
        setoran.setPetugas(petugas);
        setoran.setStatusPenjemputan(StatusPenjemputan.SEDANG_DIJEMPUT);

        Setoran saved = setoranRepository.save(setoran);
        entityManager.flush();

        if (saved.getTotalHarga() != null) {
            String keterangan = "Setoran daur ulang: " + (saved.getKategori() != null ? saved.getKategori().getNama() : "") +
                    " " + saved.getBeratKg() + " kg";
            bankSampahService.kreditSaldo(saved.getWarga().getId(), saved.getTotalHarga(), keterangan, saved.getId());
        }

        return saved;
    }

    public Setoran verifikasiSetoran(Long setoranId, Long petugasId, StatusSetoran status, String catatan) {
        Setoran setoran = setoranRepository.findById(setoranId)
                .orElseThrow(() -> new RuntimeException("Setoran tidak ditemukan"));
        User petugas = userRepository.findById(petugasId)
                .orElseThrow(() -> new RuntimeException("Petugas tidak ditemukan"));

        setoran.setStatus(status);
        setoran.setPetugas(petugas);
        if (catatan != null && !catatan.trim().isEmpty()) {
            setoran.setCatatan(catatan.trim());
        }

        return setoranRepository.save(setoran);
    }

    public Setoran verifikasiPembayaran(Long setoranId, Long petugasId, StatusPembayaran statusPembayaran, String catatan) {
        Setoran setoran = setoranRepository.findById(setoranId)
                .orElseThrow(() -> new RuntimeException("Setoran tidak ditemukan"));
        User petugas = userRepository.findById(petugasId)
                .orElseThrow(() -> new RuntimeException("Petugas tidak ditemukan"));

        setoran.setStatusPembayaran(statusPembayaran);
        setoran.setPetugas(petugas);

        if (statusPembayaran == StatusPembayaran.DISETUJUI) {
            setoran.setStatus(StatusSetoran.DITERIMA);
            setoran.setStatusPenjemputan(StatusPenjemputan.SEDANG_DIJEMPUT);
        } else if (statusPembayaran == StatusPembayaran.DITOLAK) {
            setoran.setStatus(StatusSetoran.DITOLAK);
        }

        if (catatan != null && !catatan.trim().isEmpty()) {
            setoran.setCatatan(catatan.trim());
        }

        return setoranRepository.save(setoran);
    }

    public Setoran updateStatusPenjemputan(Long setoranId, StatusPenjemputan statusPenjemputan) {
        Setoran setoran = setoranRepository.findById(setoranId)
                .orElseThrow(() -> new RuntimeException("Setoran tidak ditemukan"));

        setoran.setStatusPenjemputan(statusPenjemputan);

        if (statusPenjemputan == StatusPenjemputan.SELESAI) {
            setoran.setStatus(StatusSetoran.DITERIMA);
        }

        return setoranRepository.save(setoran);
    }

    public List<Setoran> getAllSetoranSampah() {
        return setoranRepository.findByJenisSetoranOrderByCreatedAtDesc(JenisSetoran.SAMPAH);
    }

    public List<Setoran> getSetoranByWarga(Long wargaId) {
        return setoranRepository.findByWargaIdOrderByCreatedAtDesc(wargaId);
    }

    public List<Setoran> getSetoranByStatus(StatusSetoran status) {
        return setoranRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Setoran> getSetoranByPetugas(Long petugasId) {
        return setoranRepository.findByPetugasIdOrderByCreatedAtDesc(petugasId);
    }

    public List<Setoran> getSetoranByStatusPembayaran(StatusPembayaran statusPembayaran) {
        return setoranRepository.findByStatusPembayaranOrderByCreatedAtDesc(statusPembayaran);
    }

    public Setoran createSetoranUang(Long wargaId, BigDecimal jumlah, String jenis, String deskripsi, Long petugasId) {
        User warga = userRepository.findById(wargaId)
                .orElseThrow(() -> new RuntimeException("Warga tidak ditemukan"));

        Setoran setoran = new Setoran();
        setoran.setJenisSetoran(JenisSetoran.UANG);
        setoran.setWarga(warga);
        setoran.setJumlahUang(jumlah);
        setoran.setJenisUang(jenis);
        if (deskripsi != null && !deskripsi.trim().isEmpty()) {
            setoran.setDeskripsi(deskripsi.trim());
        }

        if (petugasId != null) {
            User petugas = userRepository.findById(petugasId)
                    .orElseThrow(() -> new RuntimeException("Petugas tidak ditemukan"));
            setoran.setPetugas(petugas);
        }

        return setoranRepository.save(setoran);
    }

    public List<Setoran> getAllSetoranUang() {
        return setoranRepository.findByJenisSetoranOrderByCreatedAtDesc(JenisSetoran.UANG);
    }

    public List<Setoran> getSetoranUangByWarga(Long wargaId) {
        return setoranRepository.findByJenisSetoranAndWargaIdOrderByCreatedAtDesc(JenisSetoran.UANG, wargaId);
    }

    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();

        long totalWarga = userRepository.countByRole(com.example.trashformer.model.Role.WARGA);
        long totalPetugas = userRepository.countByRole(com.example.trashformer.model.Role.PETUGAS);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long totalSetoranHariIni = setoranRepository.countByCreatedAtBetween(todayStart, todayEnd);

        long totalMenunggu = setoranRepository.countByStatus(StatusSetoran.MENUNGGU);
        long totalDiterima = setoranRepository.countByStatus(StatusSetoran.DITERIMA);
        long totalDitolak = setoranRepository.countByStatus(StatusSetoran.DITOLAK);

        long totalPembayaranMenunggu = setoranRepository.countByStatusPembayaran(StatusPembayaran.MENUNGGU_VERIFIKASI);

        Double totalBerat = setoranRepository.sumAllBeratDiterima();
        if (totalBerat == null) totalBerat = 0.0;

        Double totalSetoranUang = setoranRepository.sumJumlahUangByJenis("SETORAN");
        if (totalSetoranUang == null) totalSetoranUang = 0.0;

        stats.put("totalWarga", totalWarga);
        stats.put("totalPetugas", totalPetugas);
        stats.put("totalSetoranHariIni", totalSetoranHariIni);
        stats.put("totalMenunggu", totalMenunggu);
        stats.put("totalDiterima", totalDiterima);
        stats.put("totalDitolak", totalDitolak);
        stats.put("totalPembayaranMenunggu", totalPembayaranMenunggu);
        stats.put("totalBerat", totalBerat.longValue());
        stats.put("totalSetoranUang", totalSetoranUang.longValue());

        return stats;
    }

    public List<Object[]> getBeratPerKategori() {
        return setoranRepository.sumBeratPerKategori();
    }

    public List<Object[]> getBeratPerBulan(int year) {
        return setoranRepository.sumBeratPerBulan(year);
    }
}
