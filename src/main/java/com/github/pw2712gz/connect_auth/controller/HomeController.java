package com.github.pw2712gz.connect_auth.controller;

import com.github.pw2712gz.connect_auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles primary routes:
 * - "/" root redirect
 * - "/login" with contextual messages
 * - "/dashboard" for authenticated users
 * - "/logout-success" redirection
 */
@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request,
                        @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            log.debug("🔄 Already authenticated. Redirecting to dashboard.");
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
            log.warn("⚠️ Dashboard accessed without authentication");
            return "redirect:/login?session";
        }

        String email = principal.getAttribute("email");
        if (email == null) {
            log.error("❌ Authenticated principal missing 'email' attribute");
            return "redirect:/login?error";
        }

        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            model.addAttribute("name", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("avatarUrl", user.getAvatarUrl());
            model.addAttribute("createdAt", user.getCreatedAt());
            model.addAttribute("lastLogin", user.getLastLogin());
        }, () -> log.warn("⚠️ No user found in DB with email '{}'", email));

        log.debug("[OAuth] Principal attributes: {}", principal.getAttributes());
        return "dashboard";
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/login?logout";
    }
}
