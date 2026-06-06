package com.example.trashformer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trashformer.model.KategoriSampah;

public interface KategoriSampahRepository extends JpaRepository<KategoriSampah, Long> {
    List<KategoriSampah> findByIsDaurUlangTrue();
}
