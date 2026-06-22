package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.WeekPlanForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.WeekPlan;
import com.example.galimbureproject.repository.BatchRepository;
import com.example.galimbureproject.repository.WeekPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WeekPlanService {

    private final WeekPlanRepository weekPlanRepository;
    private final BatchRepository batchRepository;

    public WeekPlanService(WeekPlanRepository weekPlanRepository, BatchRepository batchRepository) {
        this.weekPlanRepository = weekPlanRepository;
        this.batchRepository = batchRepository;
    }

    @Transactional(readOnly = true)
    public List<WeekPlan> getAllWeekPlans() {
        return weekPlanRepository.findAllOrderedWithBatch();
    }

    @Transactional(readOnly = true)
    public List<WeekPlan> getWeekPlansForBatch(Long batchId) {
        if (batchId == null) {
            return List.of();
        }

        return weekPlanRepository.findAllByBatch_IdOrderByWeekNumberAsc(batchId);
    }

    @Transactional(readOnly = true)
    public Optional<WeekPlan> findById(Long id) {
        return weekPlanRepository.findByIdWithBatch(id);
    }

    @Transactional
    public WeekPlan createWeekPlan(WeekPlanForm form) {
        Long batchId = form.getBatchId();
        Integer weekNumber = form.getWeekNumber();
        if (batchId == null) {
            throw new IllegalArgumentException("Batch is required.");
        }

        if (weekNumber == null) {
            throw new IllegalArgumentException("Week number is required.");
        }

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Selected batch was not found."));

        if (weekPlanRepository.existsByBatch_IdAndWeekNumber(batchId, weekNumber)) {
            throw new IllegalArgumentException(
                    "Week plan for batch " + batch.getBatchYear() + ", week " + weekNumber + " already exists."
            );
        }

        WeekPlan weekPlan = new WeekPlan();
        weekPlan.setBatch(batch);
        weekPlan.setWeekNumber(weekNumber);
        weekPlan.setTask(form.getTask() == null ? "" : form.getTask().trim());
        weekPlan.setWeekStartDate(form.getWeekStartDate());
        weekPlan.setWeekEndDate(form.getWeekEndDate());
        return weekPlanRepository.save(weekPlan);
    }
}
