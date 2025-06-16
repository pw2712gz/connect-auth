package com.github.pw2712gz.connectauth.controller;

import com.github.pw2712gz.connectauth.entity.User;
import com.github.pw2712gz.connectauth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles primary web endpoints:
 * - Root redirect
 * - Login display with contextual messages
 * - Authenticated dashboard view
 * - Logout redirect
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final UserRepository userRepository;

    @GetMapping("/")
    public String root() {
        // Redirect root to login page
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            // If already logged in, redirect to dashboard
            log.debug("🔄 User already authenticated. Redirecting to dashboard.");
            return "redirect:/dashboard";
        }

        if (request.getParameter("logout") != null) {
            model.addAttribute("message", "You’ve been logged out.");
        } else if (request.getParameter("error") != null) {
            model.addAttribute("message", "Login failed. Please try again.");
        } else if (request.getParameter("session") != null) {
            model.addAttribute("message", "Your session has expired. Please log in again.");
        } else if (request.getParameter("unauthorized") != null) {
            model.addAttribute("message", "You must log in to access that page.");
        }

        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            log.warn("Accessed /dashboard with no authenticated principal");
            return "redirect:/login?session";
        }

        String email = principal.getAttribute("email");
        if (email == null) {
            log.error("Authenticated user has no email attribute");
            return "redirect:/login?error";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            model.addAttribute("name", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("avatarUrl", user.getAvatarUrl());
            model.addAttribute("createdAt", user.getCreatedAt());
            model.addAttribute("lastLogin", user.getLastLogin());
        } else {
            log.warn("User with email '{}' not found in DB", email);
        }

        log.debug("[OAuth] Principal attributes: {}", principal.getAttributes());
        return "dashboard";
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/login?logout";
    }
}
