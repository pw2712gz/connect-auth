package com.github.pw2712gz.connect_auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom OAuth2 user service that enriches the user info
 * by fetching the verified primary email from GitHub's API.
 * This ensures the 'email' attribute is always available.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = new LinkedHashMap<>(user.getAttributes());

        if ("github".equals(registrationId)) {
            String token = userRequest.getAccessToken().getTokenValue();
            String email = fetchPrimaryVerifiedEmail(token);

            if (email != null) {
                attributes.put("email", email);
            } else {
                log.warn("[GitHub] No verified primary email found from API");
            }
        }

        return new DefaultOAuth2User(user.getAuthorities(), attributes, "email");
    }

    private String fetchPrimaryVerifiedEmail(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[GitHub] Failed to fetch email: status {}", response.getStatusCode());
            return null;
        }

        if (response.getBody() != null) {
            for (Map<String, Object> emailEntry : response.getBody()) {
                Boolean primary = (Boolean) emailEntry.get("primary");
                Boolean verified = (Boolean) emailEntry.get("verified");
                if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                    return (String) emailEntry.get("email");
                }
            }
        }

        return null;
    }
}
