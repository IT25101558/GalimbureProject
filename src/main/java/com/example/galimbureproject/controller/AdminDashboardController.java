package com.example.galimbureproject.controller;

import com.example.galimbureproject.dto.BatchForm;
import com.example.galimbureproject.dto.StudentMonthPaymentBatchForm;
import com.example.galimbureproject.dto.StudentMonthPaymentEntryForm;
import com.example.galimbureproject.dto.StudentMarkBatchForm;
import com.example.galimbureproject.dto.StudentMarkEntryForm;
import com.example.galimbureproject.dto.YearPlanForm;
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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Validated
public class AdminDashboardController {

    private final RegisteredUserRepository registeredUserRepository;
    private final BatchService batchService;
    private final StudentMonthPaymentService studentMonthPaymentService;
    private final StudentMarkService studentMarkService;
    private final YearPlanService yearPlanService;
    private final MonthPlanService monthPlanService;
    private final WeekPlanService weekPlanService;

    public AdminDashboardController(
            RegisteredUserRepository registeredUserRepository,
            BatchService batchService,
            StudentMonthPaymentService studentMonthPaymentService,
            StudentMarkService studentMarkService,
            YearPlanService yearPlanService,
            MonthPlanService monthPlanService,
            WeekPlanService weekPlanService
    ) {
        this.registeredUserRepository = registeredUserRepository;
        this.batchService = batchService;
        this.studentMonthPaymentService = studentMonthPaymentService;
        this.studentMarkService = studentMarkService;
        this.yearPlanService = yearPlanService;
        this.monthPlanService = monthPlanService;
        this.weekPlanService = weekPlanService;
    }

    @GetMapping({"/admin-dashboard", "/admin_dashboard"})
    public String showAdminDashboard(
            @RequestParam(value = "yearBatchId", required = false) Long yearBatchId,
            Model model
    ) {
        populateAdminDashboardModel(model, yearBatchId);
        return "admin-dashboard";
    }

    @GetMapping({"/admin-dashboard/marks", "/admin_dashboard/marks"})
    public String showMarksPage(
            @RequestParam(value = "batchId", required = false) Long batchId,
            @RequestParam(value = "yearPlanId", required = false) Long yearPlanId,
            @RequestParam(value = "monthPlanId", required = false) Long monthPlanId,
            @RequestParam(value = "weekPlanId", required = false) Long weekPlanId,
            Model model
    ) {
        populateMarksModel(model, batchId, yearPlanId, monthPlanId, weekPlanId);
        return "marks";
    }

    @GetMapping({"/admin-dashboard/paid-status", "/admin_dashboard/paid_status"})
    public String showPaidStatusPage(
            @RequestParam(value = "batchId", required = false) Long batchId,
            @RequestParam(value = "yearPlanId", required = false) Long yearPlanId,
            @RequestParam(value = "monthPlanId", required = false) Long monthPlanId,
            Model model
    ) {
        populatePaidStatusModel(model, batchId, yearPlanId, monthPlanId);
        return "paid-Status";
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
            populateAdminDashboardModel(model, null);
            model.addAttribute("batchForm", batchForm);
            return "admin-dashboard";
        }

        try {
            Batch savedBatch = batchService.createBatch(batchForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Batch " + savedBatch.getCompactLabel() + " was created with two years."
            );
            return "redirect:/admin-dashboard?yearBatchId=" + savedBatch.getId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateAdminDashboardModel(model, null);
            model.addAttribute("batchForm", batchForm);
            return "admin-dashboard";
        }
    }

    @PostMapping({"/admin-dashboard/years", "/admin_dashboard/years"})
    public String createYear(
            @Valid @ModelAttribute("yearPlanForm") YearPlanForm yearPlanForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Check the highlighted year fields and try again.");
            populateAdminDashboardModel(model, yearPlanForm.getBatchId());
            model.addAttribute("yearPlanForm", yearPlanForm);
            return "admin-dashboard";
        }

        try {
            YearPlan savedYear = yearPlanService.createYear(yearPlanForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Year " + savedYear.getYearValue() + " was created for batch "
                            + savedYear.getBatch().getCompactLabel() + "."
            );
            return "redirect:/admin-dashboard?yearBatchId=" + savedYear.getBatch().getId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateAdminDashboardModel(model, yearPlanForm.getBatchId());
            model.addAttribute("yearPlanForm", yearPlanForm);
            return "admin-dashboard";
        }
    }

    @PostMapping({"/admin-dashboard/paid-status", "/admin_dashboard/paid_status"})
    public String saveMonthPayments(
            @Valid @ModelAttribute("studentMonthPaymentBatchForm") StudentMonthPaymentBatchForm studentMonthPaymentBatchForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long selectedMonthPlanId = studentMonthPaymentBatchForm.getMonthPlanId();
        MonthPlan selectedMonthPlan = selectedMonthPlanId == null
                ? null
                : monthPlanService.findById(selectedMonthPlanId).orElse(null);
        Long selectedYearPlanId = selectedMonthPlan != null && selectedMonthPlan.getYearPlan() != null
                ? selectedMonthPlan.getYearPlan().getId()
                : null;
        Long selectedBatchId = selectedMonthPlan != null && selectedMonthPlan.getBatch() != null
                ? selectedMonthPlan.getBatch().getId()
                : null;

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Select a month and try again.");
            populatePaidStatusModel(model, selectedBatchId, selectedYearPlanId, selectedMonthPlanId);
            model.addAttribute("studentMonthPaymentBatchForm", studentMonthPaymentBatchForm);
            return "paid-Status";
        }

        try {
            if (selectedMonthPlanId == null) {
                throw new IllegalArgumentException("Select a month.");
            }

            selectedMonthPlan = monthPlanService.findById(selectedMonthPlanId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected month was not found."));
            selectedYearPlanId = selectedMonthPlan.getYearPlan() != null ? selectedMonthPlan.getYearPlan().getId() : null;
            selectedBatchId = selectedMonthPlan.getBatch() != null ? selectedMonthPlan.getBatch().getId() : null;
            studentMonthPaymentService.saveMonthPayments(selectedMonthPlanId, studentMonthPaymentBatchForm.getEntries());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    selectedMonthPlan.getDisplayLabel() + " payment statuses were saved."
            );
            return "redirect:/admin-dashboard/paid-status?batchId="
                    + selectedBatchId
                    + "&yearPlanId=" + selectedYearPlanId
                    + "&monthPlanId=" + selectedMonthPlanId;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populatePaidStatusModel(model, selectedBatchId, selectedYearPlanId, selectedMonthPlanId);
            model.addAttribute("studentMonthPaymentBatchForm", studentMonthPaymentBatchForm);
            return "paid-Status";
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
        Long selectedMonthPlanId = selectedWeekPlan != null && selectedWeekPlan.getMonthPlan() != null
                ? selectedWeekPlan.getMonthPlan().getId()
                : null;
        Long selectedYearPlanId = selectedWeekPlan != null && selectedWeekPlan.getYearPlan() != null
                ? selectedWeekPlan.getYearPlan().getId()
                : null;
        Long selectedBatchId = selectedWeekPlan != null && selectedWeekPlan.getBatch() != null
                ? selectedWeekPlan.getBatch().getId()
                : null;

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Check the highlighted marks and try again.");
            populateMarksModel(model, selectedBatchId, selectedYearPlanId, selectedMonthPlanId, selectedWeekPlanId);
            model.addAttribute("studentMarkBatchForm", studentMarkBatchForm);
            return "marks";
        }

        try {
            if (selectedWeekPlanId == null) {
                throw new IllegalArgumentException("Select a week plan.");
            }

            selectedWeekPlan = weekPlanService.findById(selectedWeekPlanId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected week plan was not found."));
            selectedMonthPlanId = selectedWeekPlan.getMonthPlan() != null ? selectedWeekPlan.getMonthPlan().getId() : null;
            selectedYearPlanId = selectedWeekPlan.getYearPlan() != null ? selectedWeekPlan.getYearPlan().getId() : null;
            selectedBatchId = selectedWeekPlan.getBatch() != null ? selectedWeekPlan.getBatch().getId() : null;
            studentMarkService.saveWeekMarks(selectedWeekPlanId, studentMarkBatchForm.getEntries());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    selectedWeekPlan.getDisplayLabel() + " marks were saved."
            );
            return "redirect:/admin-dashboard/marks?batchId="
                    + selectedBatchId
                    + "&yearPlanId=" + selectedYearPlanId
                    + "&monthPlanId=" + selectedMonthPlanId
                    + "&weekPlanId=" + selectedWeekPlanId;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMarksModel(model, selectedBatchId, selectedYearPlanId, selectedMonthPlanId, selectedWeekPlanId);
            model.addAttribute("studentMarkBatchForm", studentMarkBatchForm);
            return "marks";
        }
    }

    private void populateAdminDashboardModel(Model model) {
        populateAdminDashboardModel(model, null);
    }

    private void populateAdminDashboardModel(Model model, Long selectedYearBatchId) {
        List<RegisteredUser> users = registeredUserRepository.findAllByOrderByCreatedAtDesc();
        List<Batch> batches = batchService.getAllBatches();
        Batch selectedYearBatch = selectedYearBatchId == null
                ? null
                : batchService.findById(selectedYearBatchId).orElse(null);
        long yearCount = yearPlanService.countAllYears();
        long monthCount = monthPlanService.countAllMonthPlans();
        long weekCount = weekPlanService.countAllWeekPlans();
        List<YearPlan> yearPlans = selectedYearBatch == null
                ? yearPlanService.getAllYears()
                : yearPlanService.getYearsForBatch(selectedYearBatch.getId());

        model.addAttribute("users", users);
        model.addAttribute("batches", batches);
        model.addAttribute("batchCount", batches.size());
        model.addAttribute("yearCount", yearCount);
        model.addAttribute("monthCount", monthCount);
        model.addAttribute("weekCount", weekCount);
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("adminUsers", users.stream().filter(this::isAdmin).count());
        model.addAttribute("studentUsers", users.stream().filter(user -> !isAdmin(user)).count());
        model.addAttribute("yearPlans", yearPlans);
        model.addAttribute("selectedYearBatch", selectedYearBatch);
        model.addAttribute("selectedYearBatchId", selectedYearBatch != null ? selectedYearBatch.getId() : null);

        if (!model.containsAttribute("batchForm")) {
            model.addAttribute("batchForm", new BatchForm());
        }

        if (!model.containsAttribute("yearPlanForm")) {
            model.addAttribute("yearPlanForm", buildYearPlanForm(batches, selectedYearBatchId));
        }
    }

    private void populatePaidStatusModel(
            Model model,
            Long requestedBatchId,
            Long requestedYearPlanId,
            Long requestedMonthPlanId
    ) {
        List<Batch> batches = batchService.getAllBatches();
        Batch selectedBatch = resolveSelectedBatch(batches, requestedBatchId);
        List<YearPlan> yearPlans = selectedBatch == null
                ? List.of()
                : yearPlanService.getYearsForBatch(selectedBatch.getId());
        YearPlan selectedYearPlan = resolveSelectedYearPlan(yearPlans, requestedYearPlanId);
        List<MonthPlan> monthPlans = selectedYearPlan == null
                ? List.of()
                : monthPlanService.getMonthsForYear(selectedYearPlan.getId());
        MonthPlan selectedMonthPlan = resolveSelectedMonthPlan(monthPlans, requestedMonthPlanId);
        List<RegisteredUser> students = selectedBatch == null
                ? List.of()
                : registeredUserRepository.findAllByRoleAndBatch_IdOrderByFullNameAsc(UserRole.STUDENT, selectedBatch.getId());
        List<StudentMonthPayment> monthPayments = selectedMonthPlan == null
                ? List.of()
                : studentMonthPaymentService.getPaymentsForMonth(selectedMonthPlan.getId());

        model.addAttribute("batches", batches);
        model.addAttribute("selectedBatch", selectedBatch);
        model.addAttribute("selectedBatchId", selectedBatch != null ? selectedBatch.getId() : null);
        model.addAttribute("yearPlans", yearPlans);
        model.addAttribute("selectedYearPlan", selectedYearPlan);
        model.addAttribute("selectedYearPlanId", selectedYearPlan != null ? selectedYearPlan.getId() : null);
        model.addAttribute("monthPlans", monthPlans);
        model.addAttribute("selectedMonthPlan", selectedMonthPlan);
        model.addAttribute("selectedMonthPlanId", selectedMonthPlan != null ? selectedMonthPlan.getId() : null);
        model.addAttribute("students", students);
        model.addAttribute("monthPayments", monthPayments);
        model.addAttribute("paidCount", monthPayments.stream().filter(StudentMonthPayment::isPaidStatus).count());
        model.addAttribute("studentCount", students.size());
        model.addAttribute("batchCount", batches.size());
        model.addAttribute("yearCount", yearPlans.size());
        model.addAttribute("monthCount", monthPlans.size());
        model.addAttribute("totalRecords", monthPayments.size());

        if (!model.containsAttribute("studentMonthPaymentBatchForm")) {
            model.addAttribute(
                    "studentMonthPaymentBatchForm",
                    buildMonthPaymentBatchForm(students, selectedMonthPlan, monthPayments)
            );
        }
    }

    private void populateMarksModel(
            Model model,
            Long requestedBatchId,
            Long requestedYearPlanId,
            Long requestedMonthPlanId,
            Long requestedWeekPlanId
    ) {
        List<Batch> batches = batchService.getAllBatches();
        Batch selectedBatch = resolveSelectedBatch(batches, requestedBatchId);
        List<YearPlan> yearPlans = selectedBatch == null
                ? List.of()
                : yearPlanService.getYearsForBatch(selectedBatch.getId());
        YearPlan selectedYearPlan = resolveSelectedYearPlan(yearPlans, requestedYearPlanId);
        List<MonthPlan> monthPlans = selectedYearPlan == null
                ? List.of()
                : monthPlanService.getMonthsForYear(selectedYearPlan.getId());
        MonthPlan selectedMonthPlan = resolveSelectedMonthPlan(monthPlans, requestedMonthPlanId);
        List<WeekPlan> weekPlans = selectedMonthPlan == null
                ? List.of()
                : weekPlanService.getWeekPlansForMonth(selectedMonthPlan.getId());
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
        model.addAttribute("yearPlans", yearPlans);
        model.addAttribute("selectedYearPlan", selectedYearPlan);
        model.addAttribute("selectedYearPlanId", selectedYearPlan != null ? selectedYearPlan.getId() : null);
        model.addAttribute("monthPlans", monthPlans);
        model.addAttribute("selectedMonthPlan", selectedMonthPlan);
        model.addAttribute("selectedMonthPlanId", selectedMonthPlan != null ? selectedMonthPlan.getId() : null);
        model.addAttribute("weekPlans", weekPlans);
        model.addAttribute("selectedWeekPlan", selectedWeekPlan);
        model.addAttribute("selectedWeekPlanId", selectedWeekPlan != null ? selectedWeekPlan.getId() : null);
        model.addAttribute("students", students);
        model.addAttribute("weekMarks", weekMarks);
        model.addAttribute("assignedCount", weekMarks.size());
        model.addAttribute("studentCount", students.size());
        model.addAttribute("batchCount", batches.size());
        model.addAttribute("yearCount", yearPlans.size());
        model.addAttribute("monthCount", monthPlans.size());
        model.addAttribute("weekCount", weekPlans.size());
        model.addAttribute("totalMarks", weekMarks.size());

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
                    .orElseGet(() -> selectNearestBatch(batches));
        }

        return selectNearestBatch(batches);
    }

    private YearPlan resolveSelectedYearPlan(List<YearPlan> yearPlans, Long requestedYearPlanId) {
        if (yearPlans.isEmpty()) {
            return null;
        }

        if (requestedYearPlanId != null) {
            return yearPlans.stream()
                    .filter(yearPlan -> yearPlan.getId().equals(requestedYearPlanId))
                    .findFirst()
                    .orElseGet(() -> selectNearestYearPlan(yearPlans));
        }

        return selectNearestYearPlan(yearPlans);
    }

    private MonthPlan resolveSelectedMonthPlan(List<MonthPlan> monthPlans, Long requestedMonthPlanId) {
        if (monthPlans.isEmpty()) {
            return null;
        }

        if (requestedMonthPlanId != null) {
            return monthPlans.stream()
                    .filter(monthPlan -> monthPlan.getId().equals(requestedMonthPlanId))
                    .findFirst()
                    .orElseGet(() -> selectNearestMonthPlan(monthPlans));
        }

        return selectNearestMonthPlan(monthPlans);
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

    private YearPlanForm buildYearPlanForm(List<Batch> batches, Long selectedYearBatchId) {
        YearPlanForm form = new YearPlanForm();
        Batch selectedBatch = null;
        if (selectedYearBatchId != null) {
            selectedBatch = batches.stream()
                    .filter(batch -> batch.getId().equals(selectedYearBatchId))
                    .findFirst()
                    .orElse(null);
        }

        if (selectedBatch == null && !batches.isEmpty()) {
            selectedBatch = batches.get(0);
        }

        if (selectedBatch != null) {
            form.setBatchId(selectedBatch.getId());
            Integer baseYear = selectedBatch.getBatchYear();
            form.setYearValue(baseYear == null ? LocalDate.now().getYear() : baseYear + 2);
        } else {
            form.setYearValue(LocalDate.now().getYear());
        }
        return form;
    }

    private StudentMonthPaymentBatchForm buildMonthPaymentBatchForm(
            List<RegisteredUser> students,
            MonthPlan selectedMonthPlan,
            List<StudentMonthPayment> monthPayments
    ) {
        Map<Long, Boolean> paidStatusByStudentId = monthPayments.stream()
                .collect(Collectors.toMap(
                        payment -> payment.getStudent().getId(),
                        StudentMonthPayment::isPaidStatus,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));

        StudentMonthPaymentBatchForm form = new StudentMonthPaymentBatchForm();
        form.setMonthPlanId(selectedMonthPlan != null ? selectedMonthPlan.getId() : null);

        List<StudentMonthPaymentEntryForm> entries = students.stream()
                .map(student -> {
                    StudentMonthPaymentEntryForm entry = new StudentMonthPaymentEntryForm();
                    entry.setStudentId(student.getId());
                    entry.setPaidStatus(Boolean.TRUE.equals(paidStatusByStudentId.get(student.getId())));
                    return entry;
                })
                .toList();

        form.setEntries(entries);
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

    private Batch selectNearestBatch(List<Batch> batches) {
        int currentYear = LocalDate.now().getYear();
        return batches.stream()
                .filter(batch -> batch.getBatchYear() != null)
                .min(Comparator.comparingInt((Batch batch) -> Math.abs(batch.getBatchYear() - currentYear))
                        .thenComparing(Batch::getBatchYear))
                .orElse(batches.get(0));
    }

    private YearPlan selectNearestYearPlan(List<YearPlan> yearPlans) {
        int currentYear = LocalDate.now().getYear();
        return yearPlans.stream()
                .filter(yearPlan -> yearPlan.getYearValue() != null)
                .min(Comparator.comparingInt((YearPlan yearPlan) -> Math.abs(yearPlan.getYearValue() - currentYear))
                        .thenComparing(YearPlan::getYearValue))
                .orElse(yearPlans.get(0));
    }

    private MonthPlan selectNearestMonthPlan(List<MonthPlan> monthPlans) {
        int currentMonth = LocalDate.now().getMonthValue();
        return monthPlans.stream()
                .filter(monthPlan -> monthPlan.getMonthNumber() != null)
                .min(Comparator.comparingInt((MonthPlan monthPlan) -> Math.abs(monthPlan.getMonthNumber() - currentMonth))
                        .thenComparing(MonthPlan::getMonthNumber))
                .orElse(monthPlans.get(0));
    }

    private boolean isAdmin(RegisteredUser user) {
        return user.getRole() == UserRole.ADMIN;
    }
}
