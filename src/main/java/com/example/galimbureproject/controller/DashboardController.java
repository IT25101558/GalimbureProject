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

@Controller
public class DashboardController {

    private final RegisteredUserRepository registeredUserRepository;

    public DashboardController(RegisteredUserRepository registeredUserRepository) {
        this.registeredUserRepository = registeredUserRepository;
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
