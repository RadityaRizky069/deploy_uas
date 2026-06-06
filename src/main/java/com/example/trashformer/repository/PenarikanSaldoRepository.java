package com.example.trashformer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trashformer.model.PenarikanSaldo;
import com.example.trashformer.model.StatusPenarikan;

public interface PenarikanSaldoRepository extends JpaRepository<PenarikanSaldo, Long> {
    List<PenarikanSaldo> findByStatusOrderByCreatedAtDesc(StatusPenarikan status);
    List<PenarikanSaldo> findByWargaIdOrderByCreatedAtDesc(Long wargaId);
}
