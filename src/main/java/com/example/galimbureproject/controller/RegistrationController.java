package com.example.galimbureproject.controller;

import com.example.galimbureproject.dto.RegistrationForm;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.service.BatchService;
import com.example.galimbureproject.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
public class RegistrationController {

    private final RegistrationService registrationService;
    private final BatchService batchService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public RegistrationController(
            RegistrationService registrationService,
            BatchService batchService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository
    ) {
        this.registrationService = registrationService;
        this.batchService = batchService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        return redirectFor(authentication);
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model, Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return redirectFor(authentication);
        }

        populateRegistrationModel(model, new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        if (isAuthenticated(authentication)) {
            return redirectFor(authentication);
        }

        if (bindingResult.hasErrors()) {
            populateRegistrationModel(model, registrationForm);
            return "register";
        }

        RegisteredUser registeredUser;
        try {
            registeredUser = registrationService.register(registrationForm);
        } catch (IllegalArgumentException exception) {
            String message = exception.getMessage() == null ? "Registration failed." : exception.getMessage();
            String lowerMessage = message.toLowerCase();
            if (lowerMessage.contains("email")) {
                bindingResult.rejectValue("email", "email.exists", message);
            } else if (lowerMessage.contains("batch")) {
                bindingResult.rejectValue("batchYear", "batch.notFound", message);
            } else {
                bindingResult.rejectValue("batchYear", "registration.failed", message);
            }
            populateRegistrationModel(model, registrationForm);
            return "register";
        }

        try {
            Authentication authenticated = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            registeredUser.getEmail(),
                            registrationForm.getPassword()
                    )
            );

            request.getSession(true);
            request.changeSessionId();

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticated);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Account created successfully. You are signed in."
            );
            return "redirect:/dashboard";
        } catch (AuthenticationException exception) {
            return "redirect:/login?registered";
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String redirectFor(Authentication authentication) {
        if (isAdmin(authentication)) {
            return "redirect:/admin-dashboard";
        }
        return isAuthenticated(authentication) ? "redirect:/dashboard" : "redirect:/login";
    }

    private void populateRegistrationModel(Model model, RegistrationForm registrationForm) {
        var batches = batchService.getAllBatches();
        model.addAttribute("registrationForm", registrationForm);
        model.addAttribute("batches", batches);
    }

    private boolean isAdmin(Authentication authentication) {
        return isAuthenticated(authentication)
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
