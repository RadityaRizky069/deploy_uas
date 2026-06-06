package com.example.trashformer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "setoran")
public class Setoran {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "jenis_setoran", nullable = false)
    private JenisSetoran jenisSetoran;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warga_id", nullable = false)
    private User warga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petugas_id")
    private User petugas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategori_id")
    private KategoriSampah kategori;

    @Column(name = "berat_kg", precision = 10, scale = 2)
    private BigDecimal beratKg;

    @Column(name = "total_harga", precision = 12, scale = 2)
    private BigDecimal totalHarga;

    @Enumerated(EnumType.STRING)
    private StatusSetoran status;

    @Column(name = "alamat_jemput")
    private String alamatJemput;

    @Column(name = "bukti_pembayaran")
    private String buktiPembayaran;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pembayaran")
    private StatusPembayaran statusPembayaran;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_penjemputan")
    private StatusPenjemputan statusPenjemputan;

    @Column(name = "jumlah_uang", precision = 15, scale = 2)
    private BigDecimal jumlahUang;

    @Column(name = "jenis_uang")
    private String jenisUang;

    private String deskripsi;

    private String catatan;

    @Enumerated(EnumType.STRING)
    @Column(name = "jenis_sampah")
    private JenisSampah jenisSampah;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Setoran() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.jenisSetoran == JenisSetoran.SAMPAH) {
            if (this.status == null) {
                this.status = StatusSetoran.MENUNGGU;
            }
            if (this.statusPembayaran == null) {
                this.statusPembayaran = StatusPembayaran.MENUNGGU_VERIFIKASI;
            }
            if (this.statusPenjemputan == null) {
                this.statusPenjemputan = StatusPenjemputan.DIJADWALKAN;
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JenisSetoran getJenisSetoran() {
        return jenisSetoran;
    }

    public void setJenisSetoran(JenisSetoran jenisSetoran) {
        this.jenisSetoran = jenisSetoran;
    }

    public User getWarga() {
        return warga;
    }

    public void setWarga(User warga) {
        this.warga = warga;
    }

    public User getPetugas() {
        return petugas;
    }

    public void setPetugas(User petugas) {
        this.petugas = petugas;
    }

    public KategoriSampah getKategori() {
        return kategori;
    }

    public void setKategori(KategoriSampah kategori) {
        this.kategori = kategori;
    }

    public BigDecimal getBeratKg() {
        return beratKg;
    }

    public void setBeratKg(BigDecimal beratKg) {
        this.beratKg = beratKg;
    }

    public BigDecimal getTotalHarga() {
        return totalHarga;
    }

    public void setTotalHarga(BigDecimal totalHarga) {
        this.totalHarga = totalHarga;
    }

    public StatusSetoran getStatus() {
        return status;
    }

    public void setStatus(StatusSetoran status) {
        this.status = status;
    }

    public String getAlamatJemput() {
        return alamatJemput;
    }

    public void setAlamatJemput(String alamatJemput) {
        this.alamatJemput = alamatJemput;
    }

    public String getBuktiPembayaran() {
        return buktiPembayaran;
    }

    public void setBuktiPembayaran(String buktiPembayaran) {
        this.buktiPembayaran = buktiPembayaran;
    }

    public StatusPembayaran getStatusPembayaran() {
        return statusPembayaran;
    }

    public void setStatusPembayaran(StatusPembayaran statusPembayaran) {
        this.statusPembayaran = statusPembayaran;
    }

    public StatusPenjemputan getStatusPenjemputan() {
        return statusPenjemputan;
    }

    public void setStatusPenjemputan(StatusPenjemputan statusPenjemputan) {
        this.statusPenjemputan = statusPenjemputan;
    }

    public BigDecimal getJumlahUang() {
        return jumlahUang;
    }

    public void setJumlahUang(BigDecimal jumlahUang) {
        this.jumlahUang = jumlahUang;
    }

    public String getJenisUang() {
        return jenisUang;
    }

    public void setJenisUang(String jenisUang) {
        this.jenisUang = jenisUang;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public JenisSampah getJenisSampah() {
        return jenisSampah;
    }

    public void setJenisSampah(JenisSampah jenisSampah) {
        this.jenisSampah = jenisSampah;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
