package com.example.galimbureproject.service;

import com.example.galimbureproject.model.MonthPlan;
import com.example.galimbureproject.model.YearPlan;
import com.example.galimbureproject.repository.MonthPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MonthPlanService {

    private final MonthPlanRepository monthPlanRepository;
    private final WeekPlanService weekPlanService;

    public MonthPlanService(MonthPlanRepository monthPlanRepository, WeekPlanService weekPlanService) {
        this.monthPlanRepository = monthPlanRepository;
        this.weekPlanService = weekPlanService;
    }

    @Transactional(readOnly = true)
    public List<MonthPlan> getAllMonthPlans() {
        return monthPlanRepository.findAllOrderedWithHierarchy();
    }

    @Transactional(readOnly = true)
    public long countAllMonthPlans() {
        return monthPlanRepository.count();
    }

    @Transactional(readOnly = true)
    public List<MonthPlan> getMonthsForYear(Long yearPlanId) {
        if (yearPlanId == null) {
            return List.of();
        }

        return monthPlanRepository.findAllByYearPlan_IdOrderByMonthNumberAsc(yearPlanId);
    }

    @Transactional(readOnly = true)
    public List<MonthPlan> getMonthsForBatch(Long batchId) {
        if (batchId == null) {
            return List.of();
        }

        return monthPlanRepository.findAllByBatch_IdOrderByYearPlan_YearValueAscMonthNumberAsc(batchId);
    }

    @Transactional(readOnly = true)
    public Optional<MonthPlan> findById(Long id) {
        return monthPlanRepository.findByIdWithHierarchy(id);
    }

    @Transactional
    public MonthPlan updatePaidStatus(Long monthPlanId, boolean paidStatus) {
        MonthPlan monthPlan = monthPlanRepository.findByIdWithHierarchy(monthPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Selected month was not found."));
        monthPlan.setPaidStatus(paidStatus);
        return monthPlanRepository.save(monthPlan);
    }

    @Transactional
    public List<MonthPlan> createDefaultMonthsForYear(YearPlan yearPlan) {
        if (yearPlan == null || yearPlan.getId() == null || yearPlan.getBatch() == null) {
            throw new IllegalArgumentException("Year is required.");
        }

        List<MonthPlan> existingMonths = getMonthsForYear(yearPlan.getId());
        List<MonthPlan> createdMonths = new ArrayList<>();

        for (int monthNumber = 1; monthNumber <= 12; monthNumber++) {
            final int currentMonthNumber = monthNumber;
            boolean alreadyExists = existingMonths.stream()
                    .anyMatch(monthPlan -> monthPlan.getMonthNumber() != null && monthPlan.getMonthNumber().equals(currentMonthNumber));
            if (alreadyExists) {
                MonthPlan existingMonth = existingMonths.stream()
                        .filter(monthPlan -> monthPlan.getMonthNumber() != null && monthPlan.getMonthNumber().equals(currentMonthNumber))
                        .findFirst()
                        .orElse(null);
                if (existingMonth != null) {
                    weekPlanService.createDefaultWeeksForMonth(existingMonth);
                }
                continue;
            }

            MonthPlan monthPlan = new MonthPlan();
            monthPlan.setYearPlan(yearPlan);
            monthPlan.setMonthNumber(currentMonthNumber);
            monthPlan.setPaidStatus(false);
            MonthPlan savedMonth = monthPlanRepository.save(monthPlan);
            createdMonths.add(savedMonth);
            weekPlanService.createDefaultWeeksForMonth(savedMonth);
        }

        return createdMonths;
    }
}
