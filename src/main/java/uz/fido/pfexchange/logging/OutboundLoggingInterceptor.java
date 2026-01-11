package uz.fido.pfexchange.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import uz.fido.pfexchange.config.properties.ApiLoggingProperties;
import uz.fido.pfexchange.entity.CoreExchangesLog;
import uz.fido.pfexchange.service.ApiLogService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    private final ApiLogService apiLogService;
    private final ApiLoggingProperties properties;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        if (!properties.isEnabled()) {
            return execution.execute(request, body);
        }

        String correlationId = CorrelationIdHolder.get();
        if (correlationId != null) {
            request.getHeaders().add(CORRELATION_HEADER, correlationId);
        }

        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        ClientHttpResponse response = null;
        String responseBody = null;
        int statusCode = 0;

        try {
            response = execution.execute(request, body);
            statusCode = response.getStatusCode().value();

            if (properties.isLogResponseBody()) {
                responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            }

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
                                .direction("OUT")
                                .httpMethod(request.getMethod().name())
                                .endpoint(request.getURI().toString())
                                .queryParams(request.getURI().getQuery())
                                .requestBody(
                                        properties.isLogRequestBody()
                                                ? new String(body, StandardCharsets.UTF_8)
                                                : null)
                                .httpStatus(statusCode)
                                .responseBody(responseBody)
                                .externalSystem(request.getURI().getHost())
                                .startedAt(startTime)
                                .finishedAt(endTime)
                                .durationMs(durationMs)
                                .errorMessage(errorMessage)
                                .build();

                apiLogService.log(logEntry);

            } catch (Exception e) {
                log.error("Error logging outbound request: {}", e.getMessage());
            }
        }

        return response;
    }
}
