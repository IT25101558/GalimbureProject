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
import com.example.galimbureproject.service.StudentMarkService;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.model.StudentMark;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import java.util.List;

@Controller
public class DashboardController {

    private final RegisteredUserRepository registeredUserRepository;
    private final StudentMarkService studentMarkService;

    public DashboardController(
            RegisteredUserRepository registeredUserRepository,
            StudentMarkService studentMarkService
    ) {
        this.registeredUserRepository = registeredUserRepository;
        this.studentMarkService = studentMarkService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            Authentication authentication,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        return registeredUserRepository.findByEmailIgnoreCase(authentication.getName())
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("isAdmin", user.getRole() == UserRole.ADMIN);
                    List<StudentMark> studentMarks = studentMarkService.getMarksForStudent(user.getId());
                    model.addAttribute("studentMarks", studentMarks);
                    model.addAttribute(
                            "markLabels",
                            studentMarks.stream().map(mark -> {
                                if (mark.getWeekPlan() != null && mark.getWeekPlan().getBatch() != null) {
                                    String monthLabel = mark.getWeekPlan().getMonthNumber() == null
                                            ? "Month -"
                                            : Month.of(mark.getWeekPlan().getMonthNumber())
                                            .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                                    return "B" + mark.getWeekPlan().getBatch().getBatchYear()
                                            + " Y" + (mark.getWeekPlan().getYearValue() != null ? mark.getWeekPlan().getYearValue() : "-")
                                            + " " + monthLabel
                                            + " W" + mark.getWeekNumber();
                                }

                                return "W" + mark.getWeekNumber();
                            }).toList()
                    );
                    model.addAttribute(
                            "markValues",
                            studentMarks.stream().map(StudentMark::getMark).toList()
                    );
                    model.addAttribute("hasMarks", !studentMarks.isEmpty());
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
}
