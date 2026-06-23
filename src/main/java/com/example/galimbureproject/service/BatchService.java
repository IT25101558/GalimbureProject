package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.BatchForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.repository.BatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
        return batchRepository.findAllByOrderByBatchYearAscPlaceAsc();
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

        String place = form.getPlace() == null ? "" : form.getPlace().trim();
        if (place.isEmpty()) {
            throw new IllegalArgumentException("Place is required.");
        }

        if (batchRepository.existsByBatchYearAndPlaceIgnoreCase(batchYear, place)) {
            throw new IllegalArgumentException("Batch " + batchYear + " - " + place + " already exists.");
        }

        String batchDateText = form.getBatchDate() == null ? "" : form.getBatchDate().trim();
        LocalDate batchDate;
        try {
            batchDate = LocalDate.parse(batchDateText);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format.");
        }

        Batch batch = new Batch();
        batch.setBatchYear(batchYear);
        batch.setPlace(place);
        batch.setBatchDate(batchDate);
        return batchRepository.save(batch);
    }
}
