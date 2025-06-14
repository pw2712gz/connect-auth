package com.github.pw2712gz.connectauth.security;

import com.github.pw2712gz.connectauth.entity.User;
import com.github.pw2712gz.connectauth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();

        String email = (String) attributes.get("email");
        String firstName = (String) attributes.getOrDefault("given_name", "Unknown");
        String lastName = (String) attributes.getOrDefault("family_name", "");
        String picture = (String) attributes.getOrDefault("picture", null);

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

        log.info("[OAuth] User '{}' logged in successfully", email);
        response.sendRedirect("/dashboard");
    }
}

