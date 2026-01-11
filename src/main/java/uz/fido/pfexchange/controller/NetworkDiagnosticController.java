package uz.fido.pfexchange.controller;

import static uz.fido.pfexchange.config.Authority.Codes.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.diagnostic.DiagnosticResponse;
import uz.fido.pfexchange.dto.diagnostic.TestRequest;
import uz.fido.pfexchange.repository.CustomQueryRepository;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diagnostic")
@Tag(
        name = "Tarmoq Diagnostikasi",
        description = "Tashqi xizmatlar bilan ulanishni tekshirish va diagnostika qilish API'lari")
public class NetworkDiagnosticController {

    private static final String EGOV_API_HOST = "apimgw.egov.uz";
    private static final int DNS_REACHABILITY_TIMEOUT_MS = 5000;
    private static final int LOG_TRUNCATE_LENGTH = 500;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CustomQueryRepository customQueryRepository;

    @PostMapping("/test-endpoint")
    @PreAuthorize("hasAuthority('" + INTERNAL_TEST_ENDPOINT + "')")
    @Operation(
            summary = "Tashqi endpoint ni tekshirish",
            description =
                    "Berilgan URL ga ulanishni tekshiradi: DNS resolution, tarmoq ulanishi va HTTP"
                            + " javobini qaytaradi.")
    public ResponseEntity<ResponseWrapperDto<DiagnosticResponse>> testEndpoint(
            @RequestBody TestRequest request) {

        log.info("=== Starting Diagnostic Test ===");
        log.info("Target URL: {}", request.getUrl());
        log.info("HTTP Method: {}", request.getMethod());

        DiagnosticResponse response = initializeResponse(request);
        Instant startTime = Instant.now();

        try {
            performDnsResolution(request.getUrl(), response);
            applyEgovAuthIfNeeded(request);
            performConnectivityTest(request, response);

            response.setSuccess(true);
            response.setOverallMessage("Connection successful");

        } catch (ResourceAccessException e) {
            handleNetworkError(e, response);
        } catch (RestClientResponseException e) {
            handleHttpError(e, response);
        } catch (Exception e) {
            handleGeneralError(e, response);
        } finally {
            response.setTotalDurationMs(Duration.between(startTime, Instant.now()).toMillis());
            logTestCompletion(response);
        }

        return ResponseEntity.ok(
                ResponseWrapperDto.<DiagnosticResponse>builder()
                        .code(Constants.SUCCESS)
                        .message(response.getOverallMessage())
                        .data(response)
                        .build());
    }

    // ==================== Core Test Methods ====================

    private void performDnsResolution(String url, DiagnosticResponse response) {
        String host = extractHost(url);

        try {
            InetAddress address = InetAddress.getByName(host);

            Map<String, String> dnsInfo = new HashMap<>();
            dnsInfo.put("hostname", host);
            dnsInfo.put("resolvedIp", address.getHostAddress());
            dnsInfo.put("canonicalHostname", address.getCanonicalHostName());
            dnsInfo.put(
                    "reachable", String.valueOf(address.isReachable(DNS_REACHABILITY_TIMEOUT_MS)));

            response.setDnsResolution(dnsInfo);
            log.info("DNS Resolution - Host: {}, IP: {}", host, address.getHostAddress());

        } catch (UnknownHostException e) {
            log.error("DNS Resolution Failed: {}", e.getMessage());
            response.setDnsResolution(
                    Map.of("error", "DNS_RESOLUTION_FAILED", "message", e.getMessage()));
            throw new RuntimeException("DNS resolution failed: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("DNS Check Error: {}", e.getMessage());
        }
    }

    private void performConnectivityTest(TestRequest request, DiagnosticResponse response) {
        Instant requestStart = Instant.now();

        HttpHeaders headers = buildHeaders(request);
        logRequest(request, headers);

        RestClient.RequestBodySpec requestSpec =
                restClient
                        .method(HttpMethod.valueOf(request.getMethod()))
                        .uri(request.getUrl())
                        .headers(h -> h.addAll(headers));

        if (hasBody(request.getMethod()) && request.getBody() != null) {
            requestSpec.body(request.getBody());
        }

        ResponseEntity<String> apiResponse = requestSpec.retrieve().toEntity(String.class);

        Duration requestDuration = Duration.between(requestStart, Instant.now());

        response.setHttpStatusCode(apiResponse.getStatusCode().value());
        response.setResponseHeaders(convertHeaders(apiResponse.getHeaders()));
        response.setResponseBody(apiResponse.getBody());
        response.setRequestDurationMs(requestDuration.toMillis());

        logResponse(apiResponse, requestDuration);
    }

    // ==================== Error Handlers ====================

    private void handleNetworkError(ResourceAccessException e, DiagnosticResponse response) {
        response.setSuccess(false);
        response.setErrorType("NETWORK_ERROR");
        response.setErrorMessage(e.getMessage());
        response.setErrorDetails(extractNetworkErrorDetails(e));

        log.error("=== NETWORK ERROR DETECTED ===");
        log.error("Error Type: Connection/Network Issue");
        log.error("Error Message: {}", e.getMessage());
        log.error("Root Cause: {}", e.getCause() != null ? e.getCause().getMessage() : "Unknown");
        log.error("Possible Causes:");
        log.error("  1. Proxy configuration issues");
        log.error("  2. Firewall blocking the connection");
        log.error("  3. Target server is down or unreachable");
        log.error("  4. DNS resolution problems");
        log.error("  5. SSL/TLS certificate issues");
        log.error("  6. Network timeout");
        log.error("Full Stack Trace:", e);
    }

    private void handleHttpError(RestClientResponseException e, DiagnosticResponse response) {
        response.setSuccess(false);
        response.setErrorType("HTTP_ERROR");
        response.setHttpStatusCode(e.getStatusCode().value());
        response.setErrorMessage(e.getMessage());
        response.setResponseBody(e.getResponseBodyAsString());
        response.setResponseHeaders(convertHeaders(e.getResponseHeaders()));

        log.error("=== HTTP ERROR DETECTED ===");
        log.error("Status Code: {}", e.getStatusCode().value());
        log.error("Error Message: {}", e.getMessage());
        log.error("Response Body: {}", e.getResponseBodyAsString());
        log.error("Response Headers: {}", e.getResponseHeaders());
    }

    private void handleGeneralError(Exception e, DiagnosticResponse response) {
        response.setSuccess(false);
        response.setErrorType("GENERAL_ERROR");
        response.setErrorMessage(e.getMessage());

        log.error("=== GENERAL ERROR DETECTED ===");
        log.error("Error: {}", e.getMessage());
        log.error("Stack Trace:", e);
    }

    // ==================== Helper Methods ====================

    private DiagnosticResponse initializeResponse(TestRequest request) {
        DiagnosticResponse response = new DiagnosticResponse();
        response.setRequestedUrl(request.getUrl());
        response.setRequestMethod(request.getMethod());
        response.setTimestamp(Instant.now().toString());
        return response;
    }

    private void applyEgovAuthIfNeeded(TestRequest request) {
        if (!request.getUrl().contains(EGOV_API_HOST)) {
            return;
        }

        String tokenJson = customQueryRepository.getTokenJson();
        String token = extractAccessToken(tokenJson);
        if (token != null) {
            request.getHeaders().putIfAbsent("Authorization", "Bearer " + token);
        }
    }

    private HttpHeaders buildHeaders(TestRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(headers::add);
        }
        return headers;
    }

    private String extractHost(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            // Fallback to regex-based extraction
            return url.replaceAll("https?://", "").split("[:/]")[0];
        }
    }

    private Map<String, List<String>> convertHeaders(HttpHeaders headers) {
        return headers == null ? new HashMap<>() : new HashMap<>(headers);
    }

    private String extractAccessToken(String json) {
        try {
            return objectMapper.readTree(json).get("access_token").asText();
        } catch (Exception e) {
            log.warn("Failed to extract access token: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, String> extractNetworkErrorDetails(ResourceAccessException e) {
        Map<String, String> details = new HashMap<>();
        details.put("errorClass", e.getClass().getSimpleName());

        Throwable rootCause = e.getCause();
        if (rootCause == null) {
            return details;
        }

        details.put("rootCauseClass", rootCause.getClass().getSimpleName());
        details.put("rootCauseMessage", rootCause.getMessage());

        String causeMessage = rootCause.getMessage().toLowerCase();
        DiagnosisInfo diagnosis = diagnoseNetworkError(causeMessage);

        if (diagnosis != null) {
            details.put("diagnosis", diagnosis.code());
            details.put("recommendation", diagnosis.recommendation());
        }

        return details;
    }

    private DiagnosisInfo diagnoseNetworkError(String causeMessage) {
        if (causeMessage.contains("timeout")) {
            return new DiagnosisInfo(
                    "CONNECTION_TIMEOUT",
                    "Check network latency, increase timeout, or verify proxy settings");
        }
        if (causeMessage.contains("connection refused")) {
            return new DiagnosisInfo(
                    "CONNECTION_REFUSED",
                    "Target service may be down or firewall is blocking connection");
        }
        if (causeMessage.contains("unknown host")
                || causeMessage.contains("nodename nor servname provided")) {
            return new DiagnosisInfo("DNS_FAILURE", "Check DNS configuration or hostname spelling");
        }
        if (causeMessage.contains("certificate") || causeMessage.contains("ssl")) {
            return new DiagnosisInfo(
                    "SSL_CERTIFICATE_ERROR",
                    "Check SSL certificate validity and trust store configuration");
        }
        if (causeMessage.contains("proxy")) {
            return new DiagnosisInfo("PROXY_ERROR", "Verify proxy configuration and credentials");
        }
        return null;
    }

    // ==================== Logging Helpers ====================

    private void logRequest(TestRequest request, HttpHeaders headers) {
        log.info("Sending {} request to {}", request.getMethod(), request.getUrl());
        log.info("Request Headers: {}", headers);
        if (request.getBody() != null) {
            log.info("Request Body: {}", request.getBody());
        }
    }

    private void logResponse(ResponseEntity<String> response, Duration duration) {
        log.info("Response Status: {}", response.getStatusCode());
        log.info("Response Headers: {}", response.getHeaders());
        log.info("Response Duration: {} ms", duration.toMillis());
        log.info("Response Body: {}", truncateForLog(response.getBody()));
    }

    private void logTestCompletion(DiagnosticResponse response) {
        log.info("=== Diagnostic Test Completed ===");
        log.info("Success: {}", response.isSuccess());
        log.info("Duration: {} ms", response.getTotalDurationMs());
        log.info("Status Code: {}", response.getHttpStatusCode());
    }

    private String truncateForLog(String content) {
        if (content == null) return "null";
        if (content.length() <= LOG_TRUNCATE_LENGTH) return content;
        return content.substring(0, LOG_TRUNCATE_LENGTH) + "... (truncated)";
    }

    // ==================== Inner Records ====================

    private record DiagnosisInfo(String code, String recommendation) {}

    private boolean hasBody(String method) {
        return switch (method.toUpperCase()) {
            case "POST", "PUT", "PATCH" -> true;
            default -> false;
        };
    }
}
