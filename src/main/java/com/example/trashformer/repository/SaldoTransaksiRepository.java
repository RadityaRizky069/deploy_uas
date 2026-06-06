package com.example.trashformer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trashformer.model.SaldoTransaksi;

public interface SaldoTransaksiRepository extends JpaRepository<SaldoTransaksi, Long> {
    List<SaldoTransaksi> findByUserIdOrderByCreatedAtDesc(Long userId);
}
