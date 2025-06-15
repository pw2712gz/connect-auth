package com.github.pw2712gz.connectauth.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimiterConfig {

    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW_MILLIS = 60_000; // 1 minute

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
                if (now - bucket.startTime > TIME_WINDOW_MILLIS) {
                    bucket.startTime = now;
                    bucket.count = 1;
                } else {
                    bucket.count++;
                }

                if (bucket.count > MAX_REQUESTS) {
                    response.setStatus(429);
                    response.getWriter().write("Too Many Requests");
                    return;
                }
            }

            chain.doFilter(req, res);
        });

        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    private static class RequestBucket {
        long startTime = Instant.now().toEpochMilli();
        int count = 0;
    }
}
