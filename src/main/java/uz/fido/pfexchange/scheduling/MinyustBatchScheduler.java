package uz.fido.pfexchange.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.fido.pfexchange.dto.minyust.ProcessingResult;
import uz.fido.pfexchange.service.minyust.MinyustFamilyBatchRequestProcessor;

@Slf4j
@Component
@EnableScheduling
@ConditionalOnProperty(
    prefix = "minyust.batch",
    name = "scheduled-enabled",
    havingValue = "true"
)
@RequiredArgsConstructor
public class MinyustBatchScheduler {

    private final MinyustFamilyBatchRequestProcessor processor;

    /**
     * Scheduled batch processor - runs every 2 minutes
     * Only active when minyust.batch.scheduled-enabled=true
     */
    @Scheduled(fixedDelayString = "${minyust.batch.schedule-interval-ms}")
    public void scheduledBatchExecution() {
        log.info("Scheduled batch execution triggered");

        ProcessingResult result = processor.processOneBatch();

        if (result.isSuccess()) {
            if (result.getMessage().contains("No records")) {
                log.info(
                    "Scheduled processing complete - no more records to process"
                );
            } else {
                log.info("Scheduled batch completed: {}", result.getMessage());
            }
        } else {
            log.warn("Scheduled batch failed: {}", result.getMessage());
        }
    }

    /**
     * Recovery job - runs every 5 minutes
     * Resets stuck PROCESSING records back to READY
     */
    @Scheduled(fixedDelayString = "${minyust.batch.recovery-interval-ms}")
    public void scheduledRecovery() {
        log.debug("Running scheduled recovery check");

        int recovered = processor.recoverStuckRecords();

        if (recovered > 0) {
            log.warn(
                "Scheduled recovery: {} stuck records reset to READY",
                recovered
            );
        }
    }
}
