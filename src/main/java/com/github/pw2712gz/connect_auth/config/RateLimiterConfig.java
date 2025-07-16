package com.github.pw2712gz.connect_auth.config;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP-based in-memory rate limiter.
 * Limits each IP to 100 requests per minute.
 */
@Configuration
public class RateLimiterConfig {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long TIME_WINDOW_MILLIS = 60_000L;

    private final Map<String, RequestBucket> buckets = new ConcurrentHashMap<>();

    @Bean
    public FilterRegistrationBean<Filter> rateLimitingFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();

        registration.setFilter((req, res, chain) -> {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            String ip = request.getRemoteAddr();

            RequestBucket bucket = buckets.computeIfAbsent(ip, k -> new RequestBucket());

            synchronized (bucket) {
                long now = Instant.now().toEpochMilli();

                // Reset time window
                if (now - bucket.startTime > TIME_WINDOW_MILLIS) {
                    bucket.startTime = now;
                    bucket.count = 1;
                } else {
                    bucket.count++;
                }

                // Exceeding limit
                if (bucket.count > MAX_REQUESTS_PER_MINUTE) {
                    response.setStatus(429);
                    response.getWriter().write("Too Many Requests");
                    return;
                }
            }

            chain.doFilter(req, res);
        });

        registration.addUrlPatterns("/*");
        registration.setOrder(1); // Run early
        return registration;
    }

    private static class RequestBucket {
        long startTime = Instant.now().toEpochMilli();
        int count = 0;
    }
}
