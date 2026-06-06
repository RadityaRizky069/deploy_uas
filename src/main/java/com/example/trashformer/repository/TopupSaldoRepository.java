package com.example.trashformer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trashformer.model.StatusTopup;
import com.example.trashformer.model.TopupSaldo;

public interface TopupSaldoRepository extends JpaRepository<TopupSaldo, Long> {
    List<TopupSaldo> findByStatusOrderByCreatedAtDesc(StatusTopup status);
    List<TopupSaldo> findByWargaIdOrderByCreatedAtDesc(Long wargaId);
}
