package com.example.galimbureproject.controller;

import com.example.galimbureproject.dto.BatchForm;
import com.example.galimbureproject.dto.StudentMarkBatchForm;
import com.example.galimbureproject.dto.StudentMarkEntryForm;
import com.example.galimbureproject.dto.WeekPlanForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.StudentMark;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.model.WeekPlan;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import com.example.galimbureproject.service.BatchService;
import com.example.galimbureproject.service.StudentMarkService;
import com.example.galimbureproject.service.WeekPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Validated
public class AdminDashboardController {

    private final RegisteredUserRepository registeredUserRepository;
    private final BatchService batchService;
    private final StudentMarkService studentMarkService;
    private final WeekPlanService weekPlanService;

    public AdminDashboardController(
            RegisteredUserRepository registeredUserRepository,
            BatchService batchService,
            StudentMarkService studentMarkService,
            WeekPlanService weekPlanService
    ) {
        this.registeredUserRepository = registeredUserRepository;
        this.batchService = batchService;
        this.studentMarkService = studentMarkService;
        this.weekPlanService = weekPlanService;
    }

    @GetMapping({"/admin-dashboard", "/admin_dashboard"})
    public String showAdminDashboard(Model model) {
        populateAdminDashboardModel(model);
        return "admin-dashboard";
    }

    @GetMapping({"/admin-dashboard/marks", "/admin_dashboard/marks"})
    public String showMarksPage(
            @RequestParam(value = "batchId", required = false) Long batchId,
            @RequestParam(value = "weekPlanId", required = false) Long weekPlanId,
            Model model
    ) {
        populateMarksModel(model, batchId, weekPlanId);
        return "marks";
    }

    @PostMapping({"/admin-dashboard/batches", "/admin_dashboard/batches"})
    public String createBatch(
            @Valid @ModelAttribute("batchForm") BatchForm batchForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Check the highlighted batch fields and try again.");
            populateAdminDashboardModel(model);
            model.addAttribute("batchForm", batchForm);
            return "admin-dashboard";
        }

        try {
            Batch savedBatch = batchService.createBatch(batchForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Batch " + savedBatch.getCompactLabel() + " was created."
            );
            return "redirect:/admin-dashboard";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateAdminDashboardModel(model);
            model.addAttribute("batchForm", batchForm);
            return "admin-dashboard";
        }
    }

    @PostMapping({"/admin-dashboard/users/{id}/role", "/admin_dashboard/users/{id}/role"})
    public String updateUserRole(
            @PathVariable Long id,
            @RequestParam("role") @NotNull UserRole role,
            RedirectAttributes redirectAttributes
    ) {
        RegisteredUser user = registeredUserRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin-dashboard";
        }

        user.setRole(role);
        registeredUserRepository.save(user);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                user.getFullName() + " was updated to " + role.name() + "."
        );
        return "redirect:/admin-dashboard";
    }

    @PostMapping({"/admin-dashboard/week-plans", "/admin_dashboard/week_plans"})
    public String createWeekPlan(
            @Valid @ModelAttribute("weekPlanForm") WeekPlanForm weekPlanForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long selectedBatchId = weekPlanForm.getBatchId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Check the highlighted week plan fields and try again.");
            populateMarksModel(model, selectedBatchId, null);
            model.addAttribute("weekPlanForm", weekPlanForm);
            return "marks";
        }

        if (!weekPlanForm.isDateRangeValid()) {
            bindingResult.rejectValue(
                    "dateRangeValid",
                    "weekPlan.dateRange",
                    "Week end date must be on or after the week start date."
            );
            model.addAttribute("errorMessage", "Check the highlighted week plan fields and try again.");
            populateMarksModel(model, selectedBatchId, null);
            model.addAttribute("weekPlanForm", weekPlanForm);
            return "marks";
        }

        try {
            WeekPlan savedWeekPlan = weekPlanService.createWeekPlan(weekPlanForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Week " + savedWeekPlan.getWeekNumber() + " was created for batch "
                            + savedWeekPlan.getBatch().getCompactLabel() + "."
            );
            return "redirect:/admin-dashboard/marks?batchId="
                    + savedWeekPlan.getBatch().getId()
                    + "&weekPlanId=" + savedWeekPlan.getId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMarksModel(model, selectedBatchId, null);
            model.addAttribute("weekPlanForm", weekPlanForm);
            return "marks";
        }
    }

    @PostMapping({"/admin-dashboard/marks", "/admin_dashboard/marks"})
    public String saveWeeklyMarks(
            @Valid @ModelAttribute("studentMarkBatchForm") StudentMarkBatchForm studentMarkBatchForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long selectedWeekPlanId = studentMarkBatchForm.getWeekPlanId();
        WeekPlan selectedWeekPlan = selectedWeekPlanId == null
                ? null
                : weekPlanService.findById(selectedWeekPlanId).orElse(null);
        Long selectedBatchId = selectedWeekPlan != null && selectedWeekPlan.getBatch() != null
                ? selectedWeekPlan.getBatch().getId()
                : null;

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Check the highlighted marks and try again.");
            populateMarksModel(model, selectedBatchId, selectedWeekPlanId);
            model.addAttribute("studentMarkBatchForm", studentMarkBatchForm);
            return "marks";
        }

        try {
            if (selectedWeekPlanId == null) {
                throw new IllegalArgumentException("Select a week plan.");
            }

            selectedWeekPlan = weekPlanService.findById(selectedWeekPlanId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected week plan was not found."));
            selectedBatchId = selectedWeekPlan.getBatch() != null ? selectedWeekPlan.getBatch().getId() : null;
            studentMarkService.saveWeekMarks(selectedWeekPlanId, studentMarkBatchForm.getEntries());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Week " + selectedWeekPlan.getWeekNumber() + " marks saved for batch "
                            + selectedWeekPlan.getBatch().getCompactLabel() + "."
            );
            return "redirect:/admin-dashboard/marks?batchId="
                    + selectedBatchId
                    + "&weekPlanId=" + selectedWeekPlanId;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMarksModel(model, selectedBatchId, selectedWeekPlanId);
            model.addAttribute("studentMarkBatchForm", studentMarkBatchForm);
            return "marks";
        }
    }

    private void populateAdminDashboardModel(Model model) {
        List<RegisteredUser> users = registeredUserRepository.findAllByOrderByCreatedAtDesc();
        List<Batch> batches = batchService.getAllBatches();

        model.addAttribute("users", users);
        model.addAttribute("batches", batches);
        model.addAttribute("batchCount", batches.size());
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("adminUsers", users.stream().filter(this::isAdmin).count());
        model.addAttribute("studentUsers", users.stream().filter(user -> !isAdmin(user)).count());

        if (!model.containsAttribute("batchForm")) {
            model.addAttribute("batchForm", new BatchForm());
        }
    }

    private void populateMarksModel(Model model, Long requestedBatchId, Long requestedWeekPlanId) {
        List<Batch> batches = batchService.getAllBatches();
        Batch selectedBatch = resolveSelectedBatch(batches, requestedBatchId);
        List<WeekPlan> weekPlans = selectedBatch == null
                ? List.of()
                : weekPlanService.getWeekPlansForBatch(selectedBatch.getId());
        WeekPlan selectedWeekPlan = resolveSelectedWeekPlan(weekPlans, requestedWeekPlanId);
        List<RegisteredUser> students = selectedBatch == null
                ? List.of()
                : registeredUserRepository.findAllByRoleAndBatch_IdOrderByFullNameAsc(
                        UserRole.STUDENT,
                        selectedBatch.getId()
                );
        List<StudentMark> weekMarks = selectedWeekPlan == null
                ? List.of()
                : studentMarkService.getMarksForWeekPlan(selectedWeekPlan.getId());

        model.addAttribute("batches", batches);
        model.addAttribute("selectedBatch", selectedBatch);
        model.addAttribute("selectedBatchId", selectedBatch != null ? selectedBatch.getId() : null);
        model.addAttribute("weekPlans", weekPlans);
        model.addAttribute("selectedWeekPlan", selectedWeekPlan);
        model.addAttribute("selectedWeekPlanId", selectedWeekPlan != null ? selectedWeekPlan.getId() : null);
        model.addAttribute("students", students);
        model.addAttribute("weekMarks", weekMarks);
        model.addAttribute("assignedCount", weekMarks.size());
        model.addAttribute("studentCount", students.size());
        model.addAttribute("batchCount", batches.size());
        model.addAttribute("weekPlanCount", weekPlans.size());
        model.addAttribute("totalMarks", weekMarks.size());

        if (!model.containsAttribute("weekPlanForm")) {
            model.addAttribute("weekPlanForm", buildWeekPlanForm(selectedBatch));
        }

        if (!model.containsAttribute("studentMarkBatchForm")) {
            model.addAttribute("studentMarkBatchForm", buildBatchForm(students, selectedWeekPlan, weekMarks));
        }
    }

    private Batch resolveSelectedBatch(List<Batch> batches, Long requestedBatchId) {
        if (batches.isEmpty()) {
            return null;
        }

        if (requestedBatchId != null) {
            return batches.stream()
                    .filter(batch -> batch.getId().equals(requestedBatchId))
                    .findFirst()
                    .orElse(batches.get(0));
        }

        return batches.get(0);
    }

    private WeekPlan resolveSelectedWeekPlan(List<WeekPlan> weekPlans, Long requestedWeekPlanId) {
        if (weekPlans.isEmpty()) {
            return null;
        }

        if (requestedWeekPlanId != null) {
            return weekPlans.stream()
                    .filter(weekPlan -> weekPlan.getId().equals(requestedWeekPlanId))
                    .findFirst()
                    .orElse(weekPlans.get(0));
        }

        return weekPlans.get(0);
    }

    private WeekPlanForm buildWeekPlanForm(Batch selectedBatch) {
        WeekPlanForm form = new WeekPlanForm();
        form.setBatchId(selectedBatch != null ? selectedBatch.getId() : null);
        return form;
    }

    private StudentMarkBatchForm buildBatchForm(
            List<RegisteredUser> students,
            WeekPlan selectedWeekPlan,
            List<StudentMark> weekMarks
    ) {
        Map<Long, Integer> marksByStudentId = weekMarks.stream()
                .collect(Collectors.toMap(
                        mark -> mark.getStudent().getId(),
                        StudentMark::getMark,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));

        StudentMarkBatchForm form = new StudentMarkBatchForm();
        form.setWeekPlanId(selectedWeekPlan != null ? selectedWeekPlan.getId() : null);

        List<StudentMarkEntryForm> entries = students.stream()
                .map(student -> {
                    StudentMarkEntryForm entry = new StudentMarkEntryForm();
                    entry.setStudentId(student.getId());
                    entry.setMark(marksByStudentId.get(student.getId()));
                    return entry;
                })
                .toList();

        form.setEntries(entries);
        return form;
    }

    private boolean isAdmin(RegisteredUser user) {
        return user.getRole() == UserRole.ADMIN;
    }
}
