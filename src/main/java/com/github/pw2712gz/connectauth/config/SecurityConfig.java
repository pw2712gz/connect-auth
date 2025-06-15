package com.github.pw2712gz.connectauth.config;

import com.github.pw2712gz.connectauth.security.CustomOAuth2UserService;
import com.github.pw2712gz.connectauth.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/logout-success",
                                "/main.css", "/css/**", "/js/**",
                                "/images/**", "/favicon.ico",
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("❌ OAuth2 login failed: {}", exception.getMessage(), exception);
                            response.sendRedirect("/login?error");
                        })
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/logout-success")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String path = request.getRequestURI();
                            log.warn("⚠️ Unauthorized access to '{}'", path);

                            if (!path.equals("/dashboard") && !path.startsWith("/oauth2")) {
                                // Likely a bad URL — send 404
                                log.info("🛑 Unknown path '{}': sending 404", path);
                                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                            } else {
                                // Valid protected route — redirect to log in
                                response.sendRedirect("/login?unauthorized");
                            }
                        })
                );

        return http.build();
    }
}
