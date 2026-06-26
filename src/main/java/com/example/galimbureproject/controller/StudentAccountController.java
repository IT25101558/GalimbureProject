package com.example.galimbureproject.controller;

import com.example.galimbureproject.dto.ChangePasswordForm;
import com.example.galimbureproject.dto.StudentProfileForm;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import com.example.galimbureproject.service.StudentAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentAccountController {

    private final RegisteredUserRepository registeredUserRepository;
    private final StudentAccountService studentAccountService;
    private final SecurityContextRepository securityContextRepository;

    public StudentAccountController(
            RegisteredUserRepository registeredUserRepository,
            StudentAccountService studentAccountService,
            SecurityContextRepository securityContextRepository
    ) {
        this.registeredUserRepository = registeredUserRepository;
        this.studentAccountService = studentAccountService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/change-profile-info")
    public String showChangeProfileInfo(Authentication authentication, Model model) {
        RegisteredUser student = resolveCurrentStudent(authentication);
        if (student == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", student);
        model.addAttribute("isAdmin", false);
        model.addAttribute("studentProfileForm", buildStudentProfileForm(student));
        return "changeProfileInfo";
    }

    @PostMapping("/change-profile-info")
    public String updateProfileInfo(
            Authentication authentication,
            @Valid @ModelAttribute("studentProfileForm") StudentProfileForm studentProfileForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        RegisteredUser student = resolveCurrentStudent(authentication);
        if (student == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", student);
            model.addAttribute("isAdmin", false);
            return "changeProfileInfo";
        }

        try {
            RegisteredUser updatedStudent = studentAccountService.updateProfile(student.getId(), studentProfileForm);
            refreshAuthentication(authentication, updatedStudent, request, response);
            redirectAttributes.addFlashAttribute("successMessage", "Profile information was updated.");
            return "redirect:/dashboard";
        } catch (IllegalArgumentException exception) {
            String message = exception.getMessage() == null ? "Profile update failed." : exception.getMessage();
            if (message.toLowerCase().contains("email")) {
                bindingResult.rejectValue("email", "email.invalid", message);
            } else {
                bindingResult.reject("profile.update.failed", message);
            }
            model.addAttribute("errorMessage", message);
            model.addAttribute("user", student);
            model.addAttribute("isAdmin", false);
            return "changeProfileInfo";
        }
    }

    @GetMapping("/change-password")
    public String showChangePassword(Authentication authentication, Model model) {
        RegisteredUser student = resolveCurrentStudent(authentication);
        if (student == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", student);
        model.addAttribute("isAdmin", false);
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "changepassword";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Authentication authentication,
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        RegisteredUser student = resolveCurrentStudent(authentication);
        if (student == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", student);
            model.addAttribute("isAdmin", false);
            return "changepassword";
        }

        try {
            studentAccountService.changePassword(student.getId(), changePasswordForm);
            redirectAttributes.addFlashAttribute("successMessage", "Password was updated.");
            return "redirect:/dashboard";
        } catch (IllegalArgumentException exception) {
            String message = exception.getMessage() == null ? "Password update failed." : exception.getMessage();
            if (message.toLowerCase().contains("current password")) {
                bindingResult.rejectValue("currentPassword", "currentPassword.invalid", message);
            } else {
                bindingResult.rejectValue("newPassword", "newPassword.invalid", message);
            }
            model.addAttribute("errorMessage", message);
            model.addAttribute("user", student);
            model.addAttribute("isAdmin", false);
            return "changepassword";
        }
    }

    private RegisteredUser resolveCurrentStudent(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return null;
        }

        return registeredUserRepository.findByEmailIgnoreCase(authentication.getName())
                .filter(student -> student.getRole() == UserRole.STUDENT)
                .orElse(null);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private StudentProfileForm buildStudentProfileForm(RegisteredUser student) {
        StudentProfileForm form = new StudentProfileForm();
        form.setFullName(student.getFullName());
        form.setEmail(student.getEmail());
        form.setPhone(student.getPhone());
        form.setAddress(student.getAddress());
        return form;
    }

    private void refreshAuthentication(
            Authentication authentication,
            RegisteredUser updatedStudent,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        updatedStudent.getEmail(),
                        updatedStudent.getPasswordHash(),
                        authentication.getAuthorities()
                )
        );
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
