package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.BatchForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.repository.BatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private BatchService batchService;

    @Test
    void createBatchAllowsSameYearWhenPlaceDiffers() {
        BatchForm form = new BatchForm();
        form.setBatchYear(2026);
        form.setPlace("Gampaha");
        form.setBatchDate("2026-06-23");

        when(batchRepository.existsByBatchYearAndPlaceIgnoreCase(2026, "Gampaha")).thenReturn(false);
        when(batchRepository.save(any(Batch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Batch saved = batchService.createBatch(form);

        assertEquals(2026, saved.getBatchYear());
        assertEquals("Gampaha", saved.getPlace());
        assertEquals(LocalDate.parse("2026-06-23"), saved.getBatchDate());
        verify(batchRepository).existsByBatchYearAndPlaceIgnoreCase(2026, "Gampaha");
        verify(batchRepository).save(any(Batch.class));
    }

    @Test
    void createBatchRejectsDuplicateBatchYearAndPlace() {
        BatchForm form = new BatchForm();
        form.setBatchYear(2026);
        form.setPlace("Gampaha");
        form.setBatchDate("2026-06-23");

        when(batchRepository.existsByBatchYearAndPlaceIgnoreCase(2026, "Gampaha")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> batchService.createBatch(form)
        );

        assertEquals("Batch 2026 - Gampaha already exists.", exception.getMessage());
        verify(batchRepository).existsByBatchYearAndPlaceIgnoreCase(2026, "Gampaha");
        verify(batchRepository, never()).save(any(Batch.class));
    }
}
