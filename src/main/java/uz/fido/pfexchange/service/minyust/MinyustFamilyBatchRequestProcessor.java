package uz.fido.pfexchange.service.minyust;

import java.util.Map;
import uz.fido.pfexchange.dto.minyust.ProcessingResult;

public interface MinyustFamilyBatchRequestProcessor {
    /**
     * Process one batch of records (manual or scheduled)
     */
    ProcessingResult processOneBatch();

    /**
     * Process all pending requests continuously (manual mode only)
     */
    void processAllPendingRequests();

    /**
     * Recover stuck PROCESSING records
     */
    int recoverStuckRecords();

    /**
     * Get count of pending records
     */
    long getPendingCount();

    /**
     * Get summary of all statuses
     */
    Map<String, Long> getStatusSummary();
}
