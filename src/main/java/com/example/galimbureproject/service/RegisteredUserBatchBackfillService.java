package com.example.galimbureproject.service;

import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.BatchRepository;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegisteredUserBatchBackfillService {

    private final RegisteredUserRepository registeredUserRepository;
    private final BatchRepository batchRepository;

    public RegisteredUserBatchBackfillService(
            RegisteredUserRepository registeredUserRepository,
            BatchRepository batchRepository
    ) {
        this.registeredUserRepository = registeredUserRepository;
        this.batchRepository = batchRepository;
    }

    @Transactional
    public void backfillMissingBatchYears() {
        List<RegisteredUser> users = registeredUserRepository.findAllByOrderByCreatedAtDesc();
        if (users.isEmpty()) {
            return;
        }

        Map<Integer, Batch> batchesByYear = new HashMap<>();
        boolean changed = false;

        for (RegisteredUser user : users) {
            if (user.getRole() == UserRole.ADMIN) {
                continue;
            }

            Integer batchYear = user.getBatchYear();
            if (batchYear == null) {
                batchYear = resolveBatchYear(user);
                user.setBatchYear(batchYear);
                changed = true;
            }

            ensureBatchExists(batchYear, batchesByYear);
        }

        if (changed) {
            registeredUserRepository.saveAll(users);
        }
    }

    private Integer resolveBatchYear(RegisteredUser student) {
        OffsetDateTime createdAt = student.getCreatedAt();
        return createdAt != null ? createdAt.getYear() : LocalDate.now().getYear();
    }

    private Batch ensureBatchExists(Integer batchYear, Map<Integer, Batch> batchesByYear) {
        return batchesByYear.computeIfAbsent(batchYear, this::resolveOrCreateBatch);
    }

    private Batch resolveOrCreateBatch(Integer batchYear) {
        return batchRepository.findByBatchYear(batchYear)
                .orElseGet(() -> createLegacyBatch(batchYear));
    }

    private Batch createLegacyBatch(Integer batchYear) {
        Batch batch = new Batch();
        batch.setBatchYear(batchYear);
        batch.setPlace("Legacy batch");
        batch.setBatchDate(LocalDate.of(batchYear, 1, 1));
        return batchRepository.save(batch);
    }
}
