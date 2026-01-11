package uz.fido.pfexchange.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import uz.fido.pfexchange.config.properties.ApiLoggingProperties;
import uz.fido.pfexchange.entity.CoreExchangesLog;
import uz.fido.pfexchange.repository.CoreExchangesLogRepository;
import uz.fido.pfexchange.service.ApiLogService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiLogServiceImpl implements ApiLogService {

    private final ApiLoggingProperties properties;
    private final CoreExchangesLogRepository repository;

    @Override
    public void log(CoreExchangesLog logEntry) {
        if (properties.isAsync()) {
            logAsync(logEntry);
        } else {
            logSync(logEntry);
        }
    }

    @Override
    public void logSync(CoreExchangesLog logEntry) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            truncateFields(logEntry);
            repository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save API log: {}", e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void logAsync(CoreExchangesLog logEntry) {
        logSync(logEntry);
    }

    private void truncateFields(CoreExchangesLog entry) {
        int maxLen = properties.getMaxBodyLength();

        if (entry.getRequestBody() != null && entry.getRequestBody().length() > maxLen) {
            entry.setRequestBody(entry.getRequestBody().substring(0, maxLen) + "...[TRUNCATED]");
        }
        if (entry.getResponseBody() != null && entry.getResponseBody().length() > maxLen) {
            entry.setResponseBody(entry.getResponseBody().substring(0, maxLen) + "...[TRUNCATED]");
        }
        if (entry.getErrorMessage() != null && entry.getErrorMessage().length() > 4000) {
            entry.setErrorMessage(entry.getErrorMessage().substring(0, 3990) + "...[TRUNCATED]");
        }
    }
}
