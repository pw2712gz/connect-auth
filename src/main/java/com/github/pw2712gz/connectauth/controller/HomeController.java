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

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final UserRepository userRepository;

    @GetMapping("/")
    public String root() {
        // 👇 Redirect to /login instead of /dashboard
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        if (request.getParameter("logout") != null) {
            model.addAttribute("message", "You’ve been logged out.");
        } else if (request.getParameter("error") != null) {
            model.addAttribute("message", "Login failed. Please try again.");
        } else if (request.getParameter("session") != null) {
            model.addAttribute("message", "Your session has expired. Please log in again.");
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
