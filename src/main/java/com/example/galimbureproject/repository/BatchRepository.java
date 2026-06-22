package com.example.galimbureproject.repository;

import com.example.galimbureproject.model.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    List<Batch> findAllByOrderByBatchYearAsc();

    Optional<Batch> findByBatchYear(Integer batchYear);

    boolean existsByBatchYear(Integer batchYear);
}
