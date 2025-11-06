package uz.fido.pfexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.diagnostic.DiagnosticResponse;
import uz.fido.pfexchange.dto.diagnostic.TestRequest;
import uz.fido.pfexchange.repository.CustomQueryRepository;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diagnostic")
public class NetworkDiagnosticController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CustomQueryRepository customQueryRepository;

    @PostMapping("/test-endpoint")
    public ResponseEntity<ResponseWrapperDto<DiagnosticResponse>> testEndpoint(
        @RequestBody TestRequest request
    ) {
        log.info("=== Starting Diagnostic Test ===");
        log.info("Target URL: {}", request.getUrl());
        log.info("HTTP Method: {}", request.getMethod());

        DiagnosticResponse response = new DiagnosticResponse();
        response.setRequestedUrl(request.getUrl());
        response.setRequestMethod(request.getMethod());
        response.setTimestamp(Instant.now().toString());

        Instant startTime = Instant.now();

        try {
            // DNS Resolution Test
            performDnsResolution(request.getUrl(), response);

            // apimgw.egov.uz
            if (request.getUrl().contains("apimgw.egov.uz")) {
                String tokenJson = customQueryRepository.getTokenJson();
                String token = extractAccessToken(tokenJson);
                request
                    .getHeaders()
                    .putIfAbsent("Authorization", "Bearer " + token);
            }

            // Network Connectivity Test
            performConnectivityTest(request, response);

            response.setSuccess(true);
            response.setOverallMessage("Connection successful");
        } catch (ResourceAccessException e) {
            handleNetworkError(e, response);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handleHttpError(e, response);
        } catch (Exception e) {
            handleGeneralError(e, response);
        } finally {
            Duration duration = Duration.between(startTime, Instant.now());
            response.setTotalDurationMs(duration.toMillis());

            log.info("=== Diagnostic Test Completed ===");
            log.info("Success: {}", response.isSuccess());
            log.info("Duration: {} ms", response.getTotalDurationMs());
            log.info("Status Code: {}", response.getHttpStatusCode());
        }

        return ResponseEntity.ok(
            ResponseWrapperDto.<DiagnosticResponse>builder()
                .code(Constants.SUCCESS)
                .message(response.getOverallMessage())
                .data(response)
                .build()
        );
    }

    private void performDnsResolution(String url, DiagnosticResponse response) {
        try {
            String host = extractHost(url);
            InetAddress address = InetAddress.getByName(host);

            Map<String, String> dnsInfo = new HashMap<>();
            dnsInfo.put("hostname", host);
            dnsInfo.put("resolvedIp", address.getHostAddress());
            dnsInfo.put("canonicalHostname", address.getCanonicalHostName());
            dnsInfo.put("reachable", String.valueOf(address.isReachable(5000)));

            response.setDnsResolution(dnsInfo);

            log.info(
                "DNS Resolution - Host: {}, IP: {}",
                host,
                address.getHostAddress()
            );
        } catch (UnknownHostException e) {
            log.error("DNS Resolution Failed: {}", e.getMessage());
            Map<String, String> dnsError = new HashMap<>();
            dnsError.put("error", "DNS_RESOLUTION_FAILED");
            dnsError.put("message", e.getMessage());
            response.setDnsResolution(dnsError);
            throw new RuntimeException(
                "DNS resolution failed: " + e.getMessage(),
                e
            );
        } catch (Exception e) {
            log.error("DNS Check Error: {}", e.getMessage());
        }
    }

    private void performConnectivityTest(
        TestRequest request,
        DiagnosticResponse response
    ) {
        Instant requestStart = Instant.now();

        HttpHeaders headers = new HttpHeaders();
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(headers::add);
        }

        HttpEntity<String> entity = new HttpEntity<>(
            request.getBody(),
            headers
        );

        log.info(
            "Sending {} request to {}",
            request.getMethod(),
            request.getUrl()
        );
        log.info("Request Headers: {}", headers);
        if (request.getBody() != null) {
            log.info("Request Body: {}", request.getBody());
        }

        ResponseEntity<String> apiResponse = restTemplate.exchange(
            request.getUrl(),
            HttpMethod.valueOf(request.getMethod()),
            entity,
            String.class
        );

        Duration requestDuration = Duration.between(
            requestStart,
            Instant.now()
        );

        response.setHttpStatusCode(apiResponse.getStatusCode().value());
        response.setResponseHeaders(convertHeaders(apiResponse.getHeaders()));
        response.setResponseBody(apiResponse.getBody());
        response.setRequestDurationMs(requestDuration.toMillis());

        log.info("Response Status: {}", apiResponse.getStatusCode());
        log.info("Response Headers: {}", apiResponse.getHeaders());
        log.info("Response Duration: {} ms", requestDuration.toMillis());
        log.info(
            "Response Body: {}",
            truncateForLog(apiResponse.getBody(), 500)
        );
    }

    private void handleNetworkError(
        ResourceAccessException e,
        DiagnosticResponse response
    ) {
        response.setSuccess(false);
        response.setErrorType("NETWORK_ERROR");
        response.setErrorMessage(e.getMessage());
        response.setErrorDetails(extractNetworkErrorDetails(e));

        log.error("=== NETWORK ERROR DETECTED ===");
        log.error("Error Type: Connection/Network Issue");
        log.error("Error Message: {}", e.getMessage());
        log.error(
            "Root Cause: {}",
            e.getCause() != null ? e.getCause().getMessage() : "Unknown"
        );
        log.error("Possible Causes:");
        log.error("  1. Proxy configuration issues");
        log.error("  2. Firewall blocking the connection");
        log.error("  3. Target server is down or unreachable");
        log.error("  4. DNS resolution problems");
        log.error("  5. SSL/TLS certificate issues");
        log.error("  6. Network timeout");
        log.error("Full Stack Trace:", e);
    }

    private void handleHttpError(Exception e, DiagnosticResponse response) {
        int statusCode = 0;
        String responseBody = "";
        HttpHeaders responseHeaders = new HttpHeaders();

        if (e instanceof HttpClientErrorException) {
            HttpClientErrorException clientError = (HttpClientErrorException) e;
            statusCode = clientError.getStatusCode().value();
            responseBody = clientError.getResponseBodyAsString();
            responseHeaders = clientError.getResponseHeaders();
        } else if (e instanceof HttpServerErrorException) {
            HttpServerErrorException serverError = (HttpServerErrorException) e;
            statusCode = serverError.getStatusCode().value();
            responseBody = serverError.getResponseBodyAsString();
            responseHeaders = serverError.getResponseHeaders();
        }

        response.setSuccess(false);
        response.setErrorType("HTTP_ERROR");
        response.setHttpStatusCode(statusCode);
        response.setErrorMessage(e.getMessage());
        response.setResponseBody(responseBody);
        response.setResponseHeaders(convertHeaders(responseHeaders));

        log.error("=== HTTP ERROR DETECTED ===");
        log.error("Status Code: {}", statusCode);
        log.error("Error Message: {}", e.getMessage());
        log.error("Response Body: {}", responseBody);
        log.error("Response Headers: {}", responseHeaders);
    }

    private void handleGeneralError(Exception e, DiagnosticResponse response) {
        response.setSuccess(false);
        response.setErrorType("GENERAL_ERROR");
        response.setErrorMessage(e.getMessage());

        log.error("=== GENERAL ERROR DETECTED ===");
        log.error("Error: {}", e.getMessage());
        log.error("Stack Trace:", e);
    }

    private String extractHost(String url) {
        try {
            return url.replaceAll("https?://", "").split("[:/]")[0];
        } catch (Exception e) {
            return url;
        }
    }

    private Map<String, List<String>> convertHeaders(HttpHeaders headers) {
        if (headers == null) return new HashMap<>();
        return new HashMap<>(headers);
    }

    private String truncateForLog(String content, int maxLength) {
        if (content == null) return "null";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "... (truncated)";
    }

    private Map<String, String> extractNetworkErrorDetails(
        ResourceAccessException e
    ) {
        Map<String, String> details = new HashMap<>();
        details.put("errorClass", e.getClass().getSimpleName());

        Throwable rootCause = e.getCause();
        if (rootCause != null) {
            details.put("rootCauseClass", rootCause.getClass().getSimpleName());
            details.put("rootCauseMessage", rootCause.getMessage());

            // Specific network error detection
            String causeMessage = rootCause.getMessage().toLowerCase();
            if (causeMessage.contains("timeout")) {
                details.put("diagnosis", "CONNECTION_TIMEOUT");
                details.put(
                    "recommendation",
                    "Check network latency, increase timeout, or verify proxy settings"
                );
            } else if (causeMessage.contains("connection refused")) {
                details.put("diagnosis", "CONNECTION_REFUSED");
                details.put(
                    "recommendation",
                    "Target service may be down or firewall is blocking connection"
                );
            } else if (
                causeMessage.contains("unknown host") ||
                causeMessage.contains("nodename nor servname provided")
            ) {
                details.put("diagnosis", "DNS_FAILURE");
                details.put(
                    "recommendation",
                    "Check DNS configuration or hostname spelling"
                );
            } else if (
                causeMessage.contains("certificate") ||
                causeMessage.contains("ssl")
            ) {
                details.put("diagnosis", "SSL_CERTIFICATE_ERROR");
                details.put(
                    "recommendation",
                    "Check SSL certificate validity and trust store configuration"
                );
            } else if (causeMessage.contains("proxy")) {
                details.put("diagnosis", "PROXY_ERROR");
                details.put(
                    "recommendation",
                    "Verify proxy configuration and credentials"
                );
            }
        }

        return details;
    }

    private String extractAccessToken(String json) {
        try {
            return objectMapper.readTree(json).get("access_token").asText();
        } catch (Exception e) {
            return null;
        }
    }
}
