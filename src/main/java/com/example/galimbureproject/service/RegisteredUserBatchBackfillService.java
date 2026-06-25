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

    private static final String LEGACY_BATCH_PLACE = "Legacy batch";

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

        Map<Integer, Batch> legacyBatchesByYear = new HashMap<>();
        boolean changed = false;

        for (RegisteredUser user : users) {
            if (user.getRole() == UserRole.ADMIN) {
                continue;
            }

            Batch batch = user.getBatch();
            Integer batchYear = user.getBatchYear();
            if (batch != null) {
                Integer batchValue = batch.getBatchYear();
                if (batchYear == null || !batchYear.equals(batchValue)) {
                    user.setBatchYear(batchValue);
                    changed = true;
                }
                continue;
            }

            if (batchYear == null) {
                batchYear = resolveBatchYear(user);
                user.setBatchYear(batchYear);
                changed = true;
            }

            user.setBatch(ensureLegacyBatch(batchYear, legacyBatchesByYear));
            changed = true;
        }

        if (changed) {
            registeredUserRepository.saveAll(users);
        }
    }

    private Integer resolveBatchYear(RegisteredUser student) {
        OffsetDateTime createdAt = student.getCreatedAt();
        return createdAt != null ? createdAt.getYear() : LocalDate.now().getYear();
    }

    private Batch ensureLegacyBatch(Integer batchYear, Map<Integer, Batch> batchesByYear) {
        return batchesByYear.computeIfAbsent(batchYear, this::resolveOrCreateBatch);
    }

    private Batch resolveOrCreateBatch(Integer batchYear) {
        return batchRepository.findFirstByBatchYearAndPlaceIgnoreCaseOrderByIdAsc(batchYear, LEGACY_BATCH_PLACE)
                .orElseGet(() -> createLegacyBatch(batchYear));
    }

    private Batch createLegacyBatch(Integer batchYear) {
        Batch batch = new Batch();
        batch.setBatchYear(batchYear);
        batch.setPlace(LEGACY_BATCH_PLACE);
        batch.setBatchDate(LocalDate.of(batchYear, 1, 1).toString());
        return batchRepository.save(batch);
    }
}
