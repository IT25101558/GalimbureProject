package com.example.galimbureproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.galimbureproject.repository.RegisteredUserRepository;
import com.example.galimbureproject.service.MonthPlanService;
import com.example.galimbureproject.service.StudentMarkService;
import com.example.galimbureproject.service.StudentMonthPaymentService;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.model.MonthPlan;
import com.example.galimbureproject.model.StudentMark;
import com.example.galimbureproject.model.StudentMonthPayment;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final RegisteredUserRepository registeredUserRepository;
    private final StudentMarkService studentMarkService;
    private final StudentMonthPaymentService studentMonthPaymentService;
    private final MonthPlanService monthPlanService;

    public DashboardController(
            RegisteredUserRepository registeredUserRepository,
            StudentMarkService studentMarkService,
            StudentMonthPaymentService studentMonthPaymentService,
            MonthPlanService monthPlanService
    ) {
        this.registeredUserRepository = registeredUserRepository;
        this.studentMarkService = studentMarkService;
        this.studentMonthPaymentService = studentMonthPaymentService;
        this.monthPlanService = monthPlanService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            Authentication authentication,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response,
            @org.springframework.web.bind.annotation.RequestParam(value = "monthPlanId", required = false) Long monthPlanId
    ) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        return registeredUserRepository.findByEmailIgnoreCase(authentication.getName())
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("isAdmin", user.getRole() == UserRole.ADMIN);
                    List<MonthPlan> monthPlans = monthPlanService.getMonthsForBatch(
                            user.getBatch() != null ? user.getBatch().getId() : null
                    );
                    MonthPlan selectedMonthPlan = resolveSelectedMonthPlan(monthPlans, monthPlanId);
                    List<StudentMonthPayment> studentMonthPayments = studentMonthPaymentService.getPaymentsForStudent(user.getId());
                    Map<Long, StudentMonthPayment> paymentsByMonthId = studentMonthPayments.stream()
                            .filter(payment -> payment.getMonthPlan() != null && payment.getMonthPlan().getId() != null)
                            .collect(Collectors.toMap(
                                    payment -> payment.getMonthPlan().getId(),
                                    payment -> payment,
                                    (existing, replacement) -> replacement
                            ));
                    Set<Long> paidMonthIds = studentMonthPayments.stream()
                            .filter(StudentMonthPayment::isPaidStatus)
                            .map(StudentMonthPayment::getMonthPlan)
                            .filter(Objects::nonNull)
                            .map(MonthPlan::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    StudentMonthPayment selectedMonthPayment = selectedMonthPlan == null
                            ? null
                            : paymentsByMonthId.get(selectedMonthPlan.getId());
                    boolean marksUnlocked = selectedMonthPayment != null && selectedMonthPayment.isPaidStatus();
                    List<StudentMark> studentMarks = marksUnlocked
                            ? studentMarkService.getMarksForStudentAndMonth(user.getId(), selectedMonthPlan.getId())
                            : List.of();
                    model.addAttribute("studentMarks", studentMarks);
                    model.addAttribute("monthPlans", monthPlans);
                    model.addAttribute("selectedMonthPlan", selectedMonthPlan);
                    model.addAttribute("selectedMonthPlanId", selectedMonthPlan != null ? selectedMonthPlan.getId() : null);
                    model.addAttribute("paidMonthIds", paidMonthIds);
                    model.addAttribute("selectedMonthPayment", selectedMonthPayment);
                    model.addAttribute("marksUnlocked", marksUnlocked);
                    model.addAttribute("paymentStatusLabel", marksUnlocked ? "Paid" : "Waiting for payment");
                    model.addAttribute("paymentContextLabel", selectedMonthPlan != null ? buildMonthContextLabel(selectedMonthPlan) : null);

                    if (marksUnlocked) {
                        String graphBehaviourComment = buildGraphBehaviourComment(studentMarks);
                        String motivationComment = buildMotivationComment(studentMarks);
                        model.addAttribute(
                                "markLabels",
                                studentMarks.stream().map(mark -> buildMarkLabel(selectedMonthPlan, mark)).toList()
                        );
                        model.addAttribute(
                                "markValues",
                                studentMarks.stream().map(StudentMark::getMark).toList()
                        );
                        model.addAttribute("hasMarks", !studentMarks.isEmpty());
                        model.addAttribute("latestMark", studentMarks.isEmpty() ? null : studentMarks.get(studentMarks.size() - 1));
                        model.addAttribute(
                                "highestMark",
                                studentMarks.stream()
                                        .max(Comparator.comparingInt(mark -> mark.getMark() == null ? Integer.MIN_VALUE : mark.getMark()))
                                        .orElse(null)
                        );
                        model.addAttribute("graphBehaviourComment", graphBehaviourComment);
                        model.addAttribute("motivationComment", motivationComment);
                    } else {
                        model.addAttribute("markLabels", List.of());
                        model.addAttribute("markValues", List.of());
                        model.addAttribute("hasMarks", false);
                        model.addAttribute("latestMark", null);
                        model.addAttribute("highestMark", null);
                        model.addAttribute("graphBehaviourComment", null);
                        model.addAttribute("motivationComment", null);
                    }
                    return "dashboard";
                })
                .orElseGet(() -> {
                    new SecurityContextLogoutHandler().logout(request, response, authentication);
                    return "redirect:/login?missing";
                });
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private MonthPlan resolveSelectedMonthPlan(List<MonthPlan> monthPlans, Long requestedMonthPlanId) {
        if (monthPlans.isEmpty()) {
            return null;
        }

        if (requestedMonthPlanId != null) {
            return monthPlans.stream()
                    .filter(monthPlan -> monthPlan.getId().equals(requestedMonthPlanId))
                    .findFirst()
                    .orElse(monthPlans.get(monthPlans.size() - 1));
        }

        return monthPlans.get(monthPlans.size() - 1);
    }

    private String buildMonthContextLabel(MonthPlan monthPlan) {
        if (monthPlan == null) {
            return null;
        }

        return monthPlan.getMonthLabel();
    }

    private String buildMarkLabel(MonthPlan selectedMonthPlan, StudentMark mark) {
        String monthLabel = selectedMonthPlan != null ? selectedMonthPlan.getMonthLabel() : "Month";
        Integer weekNumber = mark.getWeekNumber();
        return monthLabel + " W" + (weekNumber != null ? weekNumber : "-");
    }

    private String buildGraphBehaviourComment(List<StudentMark> studentMarks) {
        List<Integer> values = studentMarks == null
                ? List.of()
                : studentMarks.stream()
                .map(StudentMark::getMark)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) {
            return "No marks are available for this month yet.";
        }

        boolean allEqual = values.stream().distinct().count() == 1;
        if (allEqual) {
            return "The graph is equal across the selected month.";
        }

        boolean nonDecreasing = true;
        boolean nonIncreasing = true;
        boolean anyIncrease = false;
        boolean anyDecrease = false;

        for (int index = 1; index < values.size(); index++) {
            int previous = values.get(index - 1);
            int current = values.get(index);

            if (current < previous) {
                nonDecreasing = false;
                anyDecrease = true;
            }
            if (current > previous) {
                nonIncreasing = false;
                anyIncrease = true;
            }
        }

        if (nonDecreasing && anyIncrease) {
            return "The graph has gradually increased and looks good.";
        }

        if (nonIncreasing && anyDecrease) {
            return "The graph has decreased across the selected month.";
        }

        Integer first = values.get(0);
        Integer last = values.get(values.size() - 1);
        if (last > first) {
            return "The graph has increased overall.";
        }
        if (last < first) {
            return "The graph has decreased overall.";
        }

        return "The graph is mixed but steady overall.";
    }

    private String buildMotivationComment(List<StudentMark> studentMarks) {
        List<Integer> values = studentMarks == null
                ? List.of()
                : studentMarks.stream()
                .map(StudentMark::getMark)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) {
            return "Work hard and keep building your marks.";
        }

        Integer latestMark = values.get(values.size() - 1);
        if (latestMark == null) {
            return "Work hard and keep building your marks.";
        }

        if (latestMark >= 85) {
            return "Good improvement. Keep working hard.";
        }

        if (latestMark >= 70) {
            return "Good progress. Keep working hard.";
        }

        if (latestMark >= 50) {
            return "Need improvement. Work hard and stay consistent.";
        }

        return "Need improvement. Work hard and keep practicing.";
    }
}
