package com.euni.backend.security;
 
import com.euni.backend.config.LoggingProperties;
import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private final LoggingProperties loggingProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isExcluded(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        long startTime = System.currentTimeMillis();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            log.info(">> Incoming Request: [{} {}] from user={}", 
                    request.getMethod(), request.getRequestURI(), getUsername());
            
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logApiDetails(requestWrapper, responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
            MDC.remove("traceId");
        }
    }


    private boolean isExcluded(String uri) {
        if (loggingProperties.getExcludedUrls() == null) return false;
        return loggingProperties.getExcludedUrls().stream().anyMatch(uri::startsWith);
    }

    private void logApiDetails(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        String requestBody = getBody(request.getContentAsByteArray());
        String responseBody = getBody(response.getContentAsByteArray());

        log.info("<< Outgoing Response: [{} {}] status={} duration={}ms | RequestBody: {} | ResponseBody: {}",
                method, uri, status, duration, maskBody(requestBody), maskBody(responseBody));
    }

    private String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }

    private String getBody(byte[] content) {
        if (content == null || content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private String maskBody(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }

        String masked = body;
        if (loggingProperties.getMaskedFields() != null) {
            for (String field : loggingProperties.getMaskedFields()) {
                // Mask JSON string values: "field":"value" -> "field":"****"
                masked = masked.replaceAll("(\"" + field + "\"\\s*:\\s*\")([^\"]+)(\")", "$1****$3");
                // Mask JSON non-string values: "field":value -> "field":****
                masked = masked.replaceAll("(\"" + field + "\"\\s*:\\s*)([^,}\\]\\s]+)", "$1****");
            }
        }

        // Also mask long content
        int maxLength = loggingProperties.getMaxFieldLength() > 0 ? loggingProperties.getMaxFieldLength() : 500;
        masked = masked.replaceAll("\":\"[^\"]{" + maxLength + ",}\"", "\":\"<hidden>\"");

        return masked;
    }

}
