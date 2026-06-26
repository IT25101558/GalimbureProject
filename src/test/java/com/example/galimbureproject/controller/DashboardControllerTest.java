package com.example.galimbureproject.controller;

import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.MonthPlan;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.StudentMark;
import com.example.galimbureproject.model.StudentMonthPayment;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.model.WeekPlan;
import com.example.galimbureproject.model.YearPlan;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import com.example.galimbureproject.service.MonthPlanService;
import com.example.galimbureproject.service.StudentMarkService;
import com.example.galimbureproject.service.StudentMonthPaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private RegisteredUserRepository registeredUserRepository;

    @Mock
    private StudentMarkService studentMarkService;

    @Mock
    private StudentMonthPaymentService studentMonthPaymentService;

    @Mock
    private MonthPlanService monthPlanService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    void showDashboardAddsSeparateGraphAndMotivationComments() {
        Authentication authentication = mock(Authentication.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        RegisteredUser user = new RegisteredUser();
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setFullName("Student Name");
        user.setEmail("student@example.com");
        user.setRole(UserRole.STUDENT);

        Batch batch = new Batch();
        ReflectionTestUtils.setField(batch, "id", 11L);
        batch.setBatchYear(2026);
        batch.setPlace("Gampaha");
        batch.setBatchDate("01 Jan 2026");
        user.setBatch(batch);

        YearPlan yearPlan = new YearPlan();
        ReflectionTestUtils.setField(yearPlan, "id", 21L);
        yearPlan.setBatch(batch);
        yearPlan.setYearValue(2026);

        MonthPlan monthPlan = new MonthPlan();
        ReflectionTestUtils.setField(monthPlan, "id", 31L);
        monthPlan.setYearPlan(yearPlan);
        monthPlan.setMonthNumber(6);

        StudentMonthPayment payment = new StudentMonthPayment();
        payment.setMonthPlan(monthPlan);
        payment.setPaidStatus(true);

        StudentMark firstMark = buildMark(1, 70);
        StudentMark secondMark = buildMark(2, 85);
        StudentMark latestMark = buildMark(3, 88);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("student@example.com");
        when(registeredUserRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(monthPlanService.getMonthsForBatch(11L)).thenReturn(List.of(monthPlan));
        when(studentMonthPaymentService.getPaymentsForStudent(7L)).thenReturn(List.of(payment));
        when(studentMarkService.getMarksForStudentAndMonth(7L, 31L))
                .thenReturn(List.of(firstMark, secondMark, latestMark));

        Model model = new ExtendedModelMap();
        String viewName = dashboardController.showDashboard(authentication, model, request, response, null);

        assertEquals("dashboard", viewName);
        assertEquals("The graph has gradually increased and looks good.", model.getAttribute("graphBehaviourComment"));
        assertEquals("Good improvement. Keep working hard.", model.getAttribute("motivationComment"));
    }

    private StudentMark buildMark(Integer weekNumber, Integer markValue) {
        WeekPlan weekPlan = new WeekPlan();
        weekPlan.setWeekNumber(weekNumber);

        StudentMark mark = new StudentMark();
        mark.setWeekPlan(weekPlan);
        mark.setMark(markValue);
        return mark;
    }
}
