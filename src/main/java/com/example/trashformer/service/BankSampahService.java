package com.example.trashformer.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trashformer.model.PenarikanSaldo;
import com.example.trashformer.model.SaldoTransaksi;
import com.example.trashformer.model.StatusPenarikan;
import com.example.trashformer.model.StatusTopup;
import com.example.trashformer.model.TipeTransaksi;
import com.example.trashformer.model.TopupSaldo;
import com.example.trashformer.model.User;
import com.example.trashformer.repository.PenarikanSaldoRepository;
import com.example.trashformer.repository.SaldoTransaksiRepository;
import com.example.trashformer.repository.TopupSaldoRepository;
import com.example.trashformer.repository.UserRepository;

@Service
public class BankSampahService {

    private final UserRepository userRepository;
    private final SaldoTransaksiRepository saldoTransaksiRepository;
    private final PenarikanSaldoRepository penarikanSaldoRepository;
    private final TopupSaldoRepository topupSaldoRepository;

    public BankSampahService(UserRepository userRepository,
                             SaldoTransaksiRepository saldoTransaksiRepository,
                             PenarikanSaldoRepository penarikanSaldoRepository,
                             TopupSaldoRepository topupSaldoRepository) {
        this.userRepository = userRepository;
        this.saldoTransaksiRepository = saldoTransaksiRepository;
        this.penarikanSaldoRepository = penarikanSaldoRepository;
        this.topupSaldoRepository = topupSaldoRepository;
    }

    @Transactional
    public SaldoTransaksi kreditSaldo(Long userId, BigDecimal jumlah, String keterangan, Long setoranId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        BigDecimal saldoSebelum = user.getSaldo() != null ? user.getSaldo() : BigDecimal.ZERO;
        BigDecimal saldoSesudah = saldoSebelum.add(jumlah);
        user.setSaldo(saldoSesudah);
        userRepository.save(user);

        SaldoTransaksi trx = new SaldoTransaksi();
        trx.setUser(user);
        trx.setTipe(TipeTransaksi.KREDIT);
        trx.setJumlah(jumlah);
        trx.setSaldoSebelum(saldoSebelum);
        trx.setSaldoSesudah(saldoSesudah);
        trx.setKeterangan(keterangan);
        trx.setSetoranId(setoranId);
        return saldoTransaksiRepository.save(trx);
    }

    @Transactional
    public SaldoTransaksi debitSaldo(Long userId, BigDecimal jumlah, String keterangan) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        BigDecimal saldoSebelum = user.getSaldo() != null ? user.getSaldo() : BigDecimal.ZERO;
        if (saldoSebelum.compareTo(jumlah) < 0) {
            throw new RuntimeException("Saldo tidak mencukupi");
        }
        BigDecimal saldoSesudah = saldoSebelum.subtract(jumlah);
        user.setSaldo(saldoSesudah);
        userRepository.save(user);

        SaldoTransaksi trx = new SaldoTransaksi();
        trx.setUser(user);
        trx.setTipe(TipeTransaksi.DEBIT);
        trx.setJumlah(jumlah);
        trx.setSaldoSebelum(saldoSebelum);
        trx.setSaldoSesudah(saldoSesudah);
        trx.setKeterangan(keterangan);
        return saldoTransaksiRepository.save(trx);
    }

    public BigDecimal getSaldo(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getSaldo() != null ? u.getSaldo() : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
    }

    public List<SaldoTransaksi> getRiwayatSaldo(Long userId) {
        return saldoTransaksiRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public PenarikanSaldo requestPenarikan(Long wargaId, BigDecimal jumlah, String catatan) {
        User warga = userRepository.findById(wargaId)
                .orElseThrow(() -> new RuntimeException("Warga tidak ditemukan"));
        BigDecimal saldo = warga.getSaldo() != null ? warga.getSaldo() : BigDecimal.ZERO;
        if (saldo.compareTo(jumlah) < 0) {
            throw new RuntimeException("Saldo tidak mencukupi untuk penarikan");
        }

        PenarikanSaldo p = new PenarikanSaldo();
        p.setWarga(warga);
        p.setJumlah(jumlah);
        p.setStatus(StatusPenarikan.MENUNGGU);
        if (catatan != null && !catatan.trim().isEmpty()) {
            p.setCatatan(catatan.trim());
        }
        return penarikanSaldoRepository.save(p);
    }

    @Transactional
    public PenarikanSaldo setujuiPenarikan(Long penarikanId, Long petugasId) {
        PenarikanSaldo p = penarikanSaldoRepository.findById(penarikanId)
                .orElseThrow(() -> new RuntimeException("Penarikan tidak ditemukan"));
        if (p.getStatus() != StatusPenarikan.MENUNGGU) {
            throw new RuntimeException("Penarikan sudah diproses");
        }
        User petugas = userRepository.findById(petugasId)
                .orElseThrow(() -> new RuntimeException("Petugas tidak ditemukan"));

        debitSaldo(p.getWarga().getId(), p.getJumlah(), "Penarikan saldo tunai");

        p.setPetugas(petugas);
        p.setStatus(StatusPenarikan.DISETUJUI);
        return penarikanSaldoRepository.save(p);
    }

    @Transactional
    public PenarikanSaldo tolakPenarikan(Long penarikanId, Long petugasId, String catatan) {
        PenarikanSaldo p = penarikanSaldoRepository.findById(penarikanId)
                .orElseThrow(() -> new RuntimeException("Penarikan tidak ditemukan"));
        if (p.getStatus() != StatusPenarikan.MENUNGGU) {
            throw new RuntimeException("Penarikan sudah diproses");
        }
        User petugas = userRepository.findById(petugasId)
                .orElseThrow(() -> new RuntimeException("Petugas tidak ditemukan"));

        p.setPetugas(petugas);
        p.setStatus(StatusPenarikan.DITOLAK);
        if (catatan != null && !catatan.trim().isEmpty()) {
            p.setCatatan(catatan.trim());
        }
        return penarikanSaldoRepository.save(p);
    }

    public List<PenarikanSaldo> getPenarikanByStatus(StatusPenarikan status) {
        return penarikanSaldoRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<PenarikanSaldo> getRiwayatPenarikan(Long wargaId) {
        return penarikanSaldoRepository.findByWargaIdOrderByCreatedAtDesc(wargaId);
    }

    @Transactional
    public TopupSaldo requestTopup(Long wargaId, BigDecimal jumlah, String catatan) {
        User warga = userRepository.findById(wargaId)
                .orElseThrow(() -> new RuntimeException("Warga tidak ditemukan"));
        if (jumlah == null || jumlah.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Jumlah topup harus lebih dari 0");
        }

        TopupSaldo t = new TopupSaldo();
        t.setWarga(warga);
        t.setJumlah(jumlah);
        t.setStatus(StatusTopup.MENUNGGU);
        if (catatan != null && !catatan.trim().isEmpty()) {
            t.setCatatan(catatan.trim());
        }
        return topupSaldoRepository.save(t);
    }

    @Transactional
    public TopupSaldo setujuiTopup(Long topupId, Long adminId) {
        TopupSaldo t = topupSaldoRepository.findById(topupId)
                .orElseThrow(() -> new RuntimeException("Topup tidak ditemukan"));
        if (t.getStatus() != StatusTopup.MENUNGGU) {
            throw new RuntimeException("Topup sudah diproses");
        }
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin tidak ditemukan"));

        kreditSaldo(t.getWarga().getId(), t.getJumlah(), "Topup saldo disetujui oleh admin", null);

        t.setAdmin(admin);
        t.setStatus(StatusTopup.DISETUJUI);
        return topupSaldoRepository.save(t);
    }

    @Transactional
    public TopupSaldo tolakTopup(Long topupId, Long adminId, String catatan) {
        TopupSaldo t = topupSaldoRepository.findById(topupId)
                .orElseThrow(() -> new RuntimeException("Topup tidak ditemukan"));
        if (t.getStatus() != StatusTopup.MENUNGGU) {
            throw new RuntimeException("Topup sudah diproses");
        }
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin tidak ditemukan"));

        t.setAdmin(admin);
        t.setStatus(StatusTopup.DITOLAK);
        if (catatan != null && !catatan.trim().isEmpty()) {
            t.setCatatan(catatan.trim());
        }
        return topupSaldoRepository.save(t);
    }

    public List<TopupSaldo> getTopupByStatus(StatusTopup status) {
        return topupSaldoRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<TopupSaldo> getRiwayatTopup(Long wargaId) {
        return topupSaldoRepository.findByWargaIdOrderByCreatedAtDesc(wargaId);
    }
}
