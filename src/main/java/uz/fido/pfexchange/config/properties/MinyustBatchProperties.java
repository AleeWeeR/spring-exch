package uz.fido.pfexchange.config.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Builder
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.minyust.batch")
public class MinyustBatchProperties {

    @Builder.Default
    private boolean scheduledEnabled = false;

    @Builder.Default
    private int batchSize = 1000;

    @Builder.Default
    private int threadPoolSize = 20;

    @Builder.Default
    private double rateLimitPerSecond = 40.0;

    @Builder.Default
    private long scheduleIntervalMs = 120000L;

    @Builder.Default
    private int batchTimeoutMinutes = 15;

    @Builder.Default
    private int stuckRecordThresholdMinutes = 10;

    @Builder.Default
    private long recoveryIntervalMs = 300000L;

    @Builder.Default
    private int maxRetries = 3;

    @Builder.Default
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreakerConfig {

        @Builder.Default
        private int failureThreshold = 10;

        @Builder.Default
        private long openDurationMs = 60000L;

        @Builder.Default
        private int halfOpenSuccessThreshold = 5;
    }
}
