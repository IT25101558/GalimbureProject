package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.BatchForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.repository.BatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BatchService {

    private final BatchRepository batchRepository;

    public BatchService(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    @Transactional(readOnly = true)
    public List<Batch> getAllBatches() {
        return batchRepository.findAllByOrderByBatchYearAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Batch> findById(Long id) {
        return batchRepository.findById(id);
    }

    @Transactional
    public Batch createBatch(BatchForm form) {
        Integer batchYear = form.getBatchYear();
        if (batchYear == null) {
            throw new IllegalArgumentException("Batch year is required.");
        }

        if (batchRepository.existsByBatchYear(batchYear)) {
            throw new IllegalArgumentException("Batch " + batchYear + " already exists.");
        }

        Batch batch = new Batch();
        batch.setBatchYear(batchYear);
        batch.setPlace(form.getPlace() == null ? "" : form.getPlace().trim());
        batch.setBatchDate(form.getBatchDate());
        return batchRepository.save(batch);
    }
}
