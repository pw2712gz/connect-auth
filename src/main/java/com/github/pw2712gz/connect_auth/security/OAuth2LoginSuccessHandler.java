package com.github.pw2712gz.connect_auth.security;

import com.github.pw2712gz.connect_auth.entity.User;
import com.github.pw2712gz.connect_auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Custom handler for successful OAuth2 logins.
 * Creates or updates the authenticated user in the database
 * and redirects to the dashboard.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = oauthUser.getAttributes();

        log.info("[OAuth] Login via '{}' successful", registrationId);
        log.debug("[OAuth] Attributes: {}", attributes);

        String email = (String) attributes.get("email");
        if (email == null) {
            log.error("[OAuth] Email is missing after user service. Redirecting to /login?error");
            response.sendRedirect("/login?error");
            return;
        }

        String firstName;
        String lastName;
        String picture;

        if ("github".equals(registrationId)) {
            String name = (String) attributes.get("name");
            if (name == null || name.isBlank()) {
                name = (String) attributes.get("login");
                log.warn("[GitHub] No name found. Using login: {}", name);
            }

            String[] parts = name != null ? name.split(" ", 2) : new String[]{"GitHub", "User"};
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
            picture = (String) attributes.get("avatar_url");

        } else {
            firstName = (String) attributes.getOrDefault("given_name", "Unknown");
            lastName = (String) attributes.getOrDefault("family_name", "");
            picture = (String) attributes.get("picture");
        }

        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setFirstName(firstName);
                    existing.setLastName(lastName);
                    existing.setAvatarUrl(picture);
                    existing.setLastLogin(Instant.now());
                    return existing;
                })
                .orElseGet(() -> User.builder()
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .avatarUrl(picture)
                        .enabled(true)
                        .createdAt(Instant.now())
                        .lastLogin(Instant.now())
                        .build()
                );

        userRepository.save(user);

        log.info("[OAuth] User '{}' saved/updated", email);
        response.sendRedirect("/dashboard");
    }
}
