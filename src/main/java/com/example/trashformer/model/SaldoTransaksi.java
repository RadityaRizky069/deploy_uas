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
import jakarta.persistence.Table;

@Entity
@Table(name = "saldo_transaksi")
public class SaldoTransaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipeTransaksi tipe;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal jumlah;

    @Column(name = "saldo_sebelum", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoSebelum;

    @Column(name = "saldo_sesudah", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoSesudah;

    private String keterangan;

    @Column(name = "setoran_id")
    private Long setoranId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public TipeTransaksi getTipe() { return tipe; }
    public void setTipe(TipeTransaksi tipe) { this.tipe = tipe; }
    public BigDecimal getJumlah() { return jumlah; }
    public void setJumlah(BigDecimal jumlah) { this.jumlah = jumlah; }
    public BigDecimal getSaldoSebelum() { return saldoSebelum; }
    public void setSaldoSebelum(BigDecimal saldoSebelum) { this.saldoSebelum = saldoSebelum; }
    public BigDecimal getSaldoSesudah() { return saldoSesudah; }
    public void setSaldoSesudah(BigDecimal saldoSesudah) { this.saldoSesudah = saldoSesudah; }
    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
    public Long getSetoranId() { return setoranId; }
    public void setSetoranId(Long setoranId) { this.setoranId = setoranId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
