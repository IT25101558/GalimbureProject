package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.YearPlanForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.YearPlan;
import com.example.galimbureproject.repository.BatchRepository;
import com.example.galimbureproject.repository.YearPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class YearPlanService {

    private final YearPlanRepository yearPlanRepository;
    private final BatchRepository batchRepository;
    private final MonthPlanService monthPlanService;

    public YearPlanService(
            YearPlanRepository yearPlanRepository,
            BatchRepository batchRepository,
            MonthPlanService monthPlanService
    ) {
        this.yearPlanRepository = yearPlanRepository;
        this.batchRepository = batchRepository;
        this.monthPlanService = monthPlanService;
    }

    @Transactional(readOnly = true)
    public List<YearPlan> getAllYears() {
        return yearPlanRepository.findAllOrderedWithBatch();
    }

    @Transactional(readOnly = true)
    public long countAllYears() {
        return yearPlanRepository.count();
    }

    @Transactional(readOnly = true)
    public List<YearPlan> getYearsForBatch(Long batchId) {
        if (batchId == null) {
            return List.of();
        }

        return yearPlanRepository.findAllByBatch_IdOrderByYearValueAsc(batchId);
    }

    @Transactional(readOnly = true)
    public Optional<YearPlan> findById(Long id) {
        return yearPlanRepository.findByIdWithBatch(id);
    }

    @Transactional
    public YearPlan createYear(YearPlanForm form) {
        Long batchId = form.getBatchId();
        Integer yearValue = form.getYearValue();
        if (batchId == null) {
            throw new IllegalArgumentException("Batch is required.");
        }

        if (yearValue == null) {
            throw new IllegalArgumentException("Year is required.");
        }

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Selected batch was not found."));

        if (yearPlanRepository.existsByBatch_IdAndYearValue(batchId, yearValue)) {
            throw new IllegalArgumentException(
                    "Year " + yearValue + " already exists for batch " + batch.getCompactLabel() + "."
            );
        }

        YearPlan yearPlan = new YearPlan();
        yearPlan.setBatch(batch);
        yearPlan.setYearValue(yearValue);
        YearPlan savedYear = yearPlanRepository.save(yearPlan);
        monthPlanService.createDefaultMonthsForYear(savedYear);
        return savedYear;
    }

    @Transactional
    public List<YearPlan> createDefaultYearsForBatch(Batch batch) {
        if (batch == null || batch.getId() == null || batch.getBatchYear() == null) {
            throw new IllegalArgumentException("Batch is required.");
        }

        List<YearPlan> existingYears = getYearsForBatch(batch.getId());
        List<YearPlan> createdYears = new ArrayList<>();

        for (int yearValue : List.of(batch.getBatchYear(), batch.getBatchYear() + 1)) {
            YearPlan existingYear = existingYears.stream()
                    .filter(yearPlan -> yearPlan.getYearValue() != null && yearPlan.getYearValue().equals(yearValue))
                    .findFirst()
                    .orElse(null);

            if (existingYear != null) {
                monthPlanService.createDefaultMonthsForYear(existingYear);
                continue;
            }

            YearPlan yearPlan = new YearPlan();
            yearPlan.setBatch(batch);
            yearPlan.setYearValue(yearValue);
            YearPlan savedYear = yearPlanRepository.save(yearPlan);
            createdYears.add(savedYear);
            monthPlanService.createDefaultMonthsForYear(savedYear);
        }

        return createdYears;
    }
}
