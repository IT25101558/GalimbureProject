package com.example.galimbureproject.service;

import com.example.galimbureproject.model.MonthPlan;
import com.example.galimbureproject.model.WeekPlan;
import com.example.galimbureproject.repository.WeekPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WeekPlanService {

    private final WeekPlanRepository weekPlanRepository;

    public WeekPlanService(WeekPlanRepository weekPlanRepository) {
        this.weekPlanRepository = weekPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<WeekPlan> getAllWeekPlans() {
        return weekPlanRepository.findAllOrderedWithHierarchy();
    }

    @Transactional(readOnly = true)
    public long countAllWeekPlans() {
        return weekPlanRepository.count();
    }

    @Transactional(readOnly = true)
    public List<WeekPlan> getWeekPlansForMonth(Long monthPlanId) {
        if (monthPlanId == null) {
            return List.of();
        }

        return weekPlanRepository.findAllByMonthPlan_IdOrderByWeekNumberAsc(monthPlanId);
    }

    @Transactional(readOnly = true)
    public Optional<WeekPlan> findById(Long id) {
        return weekPlanRepository.findByIdWithHierarchy(id);
    }

    @Transactional
    public List<WeekPlan> createDefaultWeeksForMonth(MonthPlan monthPlan) {
        if (monthPlan == null || monthPlan.getId() == null || monthPlan.getYearPlan() == null) {
            throw new IllegalArgumentException("Month is required.");
        }

        Integer yearValue = monthPlan.getYearValue();
        Integer monthNumber = monthPlan.getMonthNumber();
        if (yearValue == null || monthNumber == null) {
            throw new IllegalArgumentException("Month is required.");
        }

        List<WeekPlan> existingWeeks = getWeekPlansForMonth(monthPlan.getId());
        List<WeekPlan> createdWeeks = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(yearValue, monthNumber);
        LocalDate monthStart = yearMonth.atDay(1);

        for (int weekNumber = 1; weekNumber <= 5; weekNumber++) {
            final int currentWeekNumber = weekNumber;
            boolean alreadyExists = existingWeeks.stream()
                    .anyMatch(weekPlan -> weekPlan.getWeekNumber() != null && weekPlan.getWeekNumber().equals(currentWeekNumber));
            if (alreadyExists) {
                continue;
            }

            WeekPlan weekPlan = new WeekPlan();
            weekPlan.setMonthPlan(monthPlan);
            weekPlan.setWeekNumber(currentWeekNumber);
            weekPlan.setTask("Week " + currentWeekNumber);
            weekPlan.setWeekStartDate(monthStart.plusWeeks(currentWeekNumber - 1L));
            weekPlan.setWeekEndDate(monthStart.plusWeeks(currentWeekNumber - 1L).plusDays(6));
            createdWeeks.add(weekPlanRepository.save(weekPlan));
        }

        return createdWeeks;
    }
}
