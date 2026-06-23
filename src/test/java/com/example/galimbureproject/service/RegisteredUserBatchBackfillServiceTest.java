package com.example.galimbureproject.service;

import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.repository.BatchRepository;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisteredUserBatchBackfillServiceTest {

    @Mock
    private RegisteredUserRepository registeredUserRepository;

    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private RegisteredUserBatchBackfillService registeredUserBatchBackfillService;

    @Test
    void backfillPopulatesMissingBatchYearAndCreatesBatchRow() {
        RegisteredUser user = new RegisteredUser();
        user.setFullName("Legacy Student");
        ReflectionTestUtils.setField(user, "createdAt", OffsetDateTime.parse("2024-06-01T10:15:30Z"));

        when(registeredUserRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(user));
        when(batchRepository.findByBatchYear(2024)).thenReturn(Optional.empty());
        when(batchRepository.save(any(Batch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registeredUserBatchBackfillService.backfillMissingBatchYears();

        assertEquals(2024, user.getBatchYear());
        verify(batchRepository).save(any(Batch.class));
        verify(registeredUserRepository).saveAll(List.of(user));
    }
}
