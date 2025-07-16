package com.github.pw2712gz.connect_auth.config;

import com.github.pw2712gz.connect_auth.security.CustomOAuth2UserService;
import com.github.pw2712gz.connect_auth.security.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration:
 * - OAuth2 login via GitHub/Google
 * - Public/protected route handling
 * - Graceful handling of 401 and 404 for unauthenticated requests
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/", "/login",
            "/style.css",
            "/main.css", "/css/**", "/js/**",
            "/images/**", "/favicon.ico",
            "/actuator/health",
            "/oauth2/**", "/login/oauth2/**"
    };

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
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
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String path = request.getRequestURI();
                            log.warn("⚠️ Unauthorized access to '{}'", path);

                            // Let Spring handle expected OAuth2 routes
                            if (path.startsWith("/oauth2") || path.startsWith("/login/oauth2")) {
                                response.sendRedirect("/login");
                            } else if (!path.equals("/dashboard")) {
                                log.info("🛑 Unknown path '{}': sending 404", path);
                                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                            } else {
                                response.sendRedirect("/login?unauthorized");
                            }
                        })
                );

        return http.build();
    }
}
