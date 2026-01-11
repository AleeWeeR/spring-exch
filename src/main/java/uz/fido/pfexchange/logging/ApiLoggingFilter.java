package uz.fido.pfexchange.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import uz.fido.pfexchange.config.properties.ApiLoggingProperties;
import uz.fido.pfexchange.entity.CoreExchangesLog;
import uz.fido.pfexchange.service.ApiLogService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final ApiLogService apiLogService;
    private final ApiLoggingProperties properties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!properties.isEnabled() || shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        CorrelationIdHolder.set(correlationId);
        wrappedResponse.setHeader(CORRELATION_HEADER, correlationId);

        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            throw e;
        } finally {
            try {
                LocalDateTime endTime = LocalDateTime.now();
                long durationMs = ChronoUnit.MILLIS.between(startTime, endTime);

                CoreExchangesLog logEntry =
                        CoreExchangesLog.builder()
                                .correlationId(correlationId)
                                .direction("IN")
                                .httpMethod(request.getMethod())
                                .endpoint(request.getRequestURI())
                                .queryParams(request.getQueryString())
                                .requestBody(getRequestBody(wrappedRequest))
                                .httpStatus(wrappedResponse.getStatus())
                                .responseBody(getResponseBody(wrappedResponse))
                                .remoteIp(getClientIp(request))
                                .externalSystem(extractExternalSystem(request))
                                .userId(extractUserId(request))
                                .startedAt(startTime)
                                .finishedAt(endTime)
                                .durationMs(durationMs)
                                .errorMessage(errorMessage)
                                .build();

                apiLogService.log(logEntry);

            } catch (Exception e) {
                log.error("Error logging API request: {}", e.getMessage());
            } finally {
                wrappedResponse.copyBodyToResponse();
                CorrelationIdHolder.clear();
            }
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return properties.getExcludeEndpoints().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        if (!properties.isLogRequestBody()) {
            return null;
        }
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        if (!properties.isLogResponseBody()) {
            return null;
        }
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String extractExternalSystem(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/v1/pf")) {
            String[] parts = uri.split("/");
            if (parts.length > 3) {
                return parts[4];
            }
        } else if (uri.startsWith("/api/v1")) {
            String[] parts = uri.split("/");
            if (parts.length > 2) {
                return parts[3];
            }
        }

        return "none";
    }

    private String extractUserId(HttpServletRequest request) {
        return request.getHeader("X-User-ID");
    }
}
