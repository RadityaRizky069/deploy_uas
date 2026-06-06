package com.example.trashformer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "kategori_sampah")
public class KategoriSampah {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nama;

    private String deskripsi;

    @Column(name = "harga_per_kg", precision = 10, scale = 2)
    private BigDecimal hargaPerKg;

    @Column(name = "is_daur_ulang")
    private Boolean isDaurUlang = false;

    @Column(name = "harga_daur_ulang", precision = 10, scale = 2)
    private BigDecimal hargaDaurUlang;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public KategoriSampah() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public BigDecimal getHargaPerKg() {
        return hargaPerKg;
    }

    public void setHargaPerKg(BigDecimal hargaPerKg) {
        this.hargaPerKg = hargaPerKg;
    }

    public Boolean getIsDaurUlang() { return isDaurUlang; }
    public void setIsDaurUlang(Boolean isDaurUlang) { this.isDaurUlang = isDaurUlang; }
    public BigDecimal getHargaDaurUlang() { return hargaDaurUlang; }
    public void setHargaDaurUlang(BigDecimal hargaDaurUlang) { this.hargaDaurUlang = hargaDaurUlang; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
