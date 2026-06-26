package com.example.galimbureproject.controller;

import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.MonthPlan;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.StudentMonthPayment;
import com.example.galimbureproject.model.StudentMark;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.model.WeekPlan;
import com.example.galimbureproject.model.YearPlan;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import com.example.galimbureproject.service.BatchService;
import com.example.galimbureproject.service.MonthPlanService;
import com.example.galimbureproject.service.StudentMonthPaymentService;
import com.example.galimbureproject.service.StudentMarkService;
import com.example.galimbureproject.service.WeekPlanService;
import com.example.galimbureproject.service.YearPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock
    private RegisteredUserRepository registeredUserRepository;

    @Mock
    private BatchService batchService;

    @Mock
    private StudentMonthPaymentService studentMonthPaymentService;

    @Mock
    private StudentMarkService studentMarkService;

    @Mock
    private YearPlanService yearPlanService;

    @Mock
    private MonthPlanService monthPlanService;

    @Mock
    private WeekPlanService weekPlanService;

    @InjectMocks
    private AdminDashboardController adminDashboardController;

    @Test
    void showMarksPageDefaultsToCurrentDateHierarchy() {
        LocalDate today = LocalDate.now();

        Batch olderBatch = buildBatch(1L, today.getYear() - 1);
        Batch currentBatch = buildBatch(2L, today.getYear());

        YearPlan currentYear = buildYearPlan(21L, currentBatch, today.getYear());
        YearPlan nextYear = buildYearPlan(22L, currentBatch, today.getYear() + 1);

        MonthPlan currentMonth = buildMonthPlan(31L, currentYear, today.getMonthValue());
        MonthPlan nextMonth = buildMonthPlan(32L, currentYear, today.getMonthValue() == 12 ? 11 : today.getMonthValue() + 1);

        WeekPlan currentWeek = buildWeekPlan(41L, currentMonth, 1);

        when(batchService.getAllBatches()).thenReturn(List.of(olderBatch, currentBatch));
        when(yearPlanService.getYearsForBatch(2L)).thenReturn(List.of(currentYear, nextYear));
        when(monthPlanService.getMonthsForYear(21L)).thenReturn(List.of(currentMonth, nextMonth));
        when(weekPlanService.getWeekPlansForMonth(31L)).thenReturn(List.of(currentWeek));
        when(registeredUserRepository.findAllByRoleAndBatch_IdOrderByFullNameAsc(eq(UserRole.STUDENT), anyLong()))
                .thenReturn(List.of());
        when(studentMarkService.getMarksForWeekPlan(41L)).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String viewName = adminDashboardController.showMarksPage(null, null, null, null, model);

        assertEquals("marks", viewName);
        assertEquals(2L, model.getAttribute("selectedBatchId"));
        assertEquals(21L, model.getAttribute("selectedYearPlanId"));
        assertEquals(31L, model.getAttribute("selectedMonthPlanId"));
        assertEquals(41L, model.getAttribute("selectedWeekPlanId"));
    }

    private Batch buildBatch(Long id, Integer batchYear) {
        Batch batch = new Batch();
        ReflectionTestUtils.setField(batch, "id", id);
        batch.setBatchYear(batchYear);
        batch.setPlace("Gampaha");
        batch.setBatchDate("01 Jan 2026");
        return batch;
    }

    private YearPlan buildYearPlan(Long id, Batch batch, Integer yearValue) {
        YearPlan yearPlan = new YearPlan();
        ReflectionTestUtils.setField(yearPlan, "id", id);
        yearPlan.setBatch(batch);
        yearPlan.setYearValue(yearValue);
        return yearPlan;
    }

    private MonthPlan buildMonthPlan(Long id, YearPlan yearPlan, Integer monthNumber) {
        MonthPlan monthPlan = new MonthPlan();
        ReflectionTestUtils.setField(monthPlan, "id", id);
        monthPlan.setYearPlan(yearPlan);
        monthPlan.setMonthNumber(monthNumber);
        return monthPlan;
    }

    private WeekPlan buildWeekPlan(Long id, MonthPlan monthPlan, Integer weekNumber) {
        WeekPlan weekPlan = new WeekPlan();
        ReflectionTestUtils.setField(weekPlan, "id", id);
        weekPlan.setMonthPlan(monthPlan);
        weekPlan.setWeekNumber(weekNumber);
        weekPlan.setTask("Task");
        return weekPlan;
    }
}
