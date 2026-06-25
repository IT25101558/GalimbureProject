package com.example.galimbureproject.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicPageController {

    @GetMapping({"/about-us", "/about_us"})
    public String showAboutPage(Model model, Authentication authentication) {
        model.addAttribute("isAdmin", isAdmin(authentication));
        return "about_us";
    }

    @GetMapping({"/contact-us", "/contact_us"})
    public String showContactPage(Model model, Authentication authentication) {
        model.addAttribute("isAdmin", isAdmin(authentication));
        return "contact_us";
    }

    private boolean isAdmin(Authentication authentication) {
        return isAuthenticated(authentication)
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
