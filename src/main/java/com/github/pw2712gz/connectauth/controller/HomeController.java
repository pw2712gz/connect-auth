package com.github.pw2712gz.connectauth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("name", principal.getAttribute("name"));
            model.addAttribute("email", principal.getAttribute("email"));

            // Optional: log raw attributes for debugging new providers
            log.debug("[OAuth] User attributes: {}", principal.getAttributes());
        } else {
            log.warn("Accessed /dashboard with no principal");
        }
        return "dashboard";
    }

    // Optional: future logout or error pages
    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/login?logout";
    }
}
