package uz.fido.pfexchange.service.impl.minyust;

import com.google.common.util.concurrent.RateLimiter;

import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import uz.fido.pfexchange.config.properties.MinyustBatchProperties;
import uz.fido.pfexchange.dto.PersonActivityDto;
import uz.fido.pfexchange.dto.minyust.*;
import uz.fido.pfexchange.entity.minyust.Women;
import uz.fido.pfexchange.entity.minyust.WomenChildrenInf;
import uz.fido.pfexchange.repository.CustomQueryRepository;
// import uz.fido.pfexchange.repository.minyust.ExchangeMinyustFamilyInfRepository;
import uz.fido.pfexchange.repository.minyust.WomenChildrenInfRepository;
import uz.fido.pfexchange.repository.minyust.WomenRepository;
import uz.fido.pfexchange.service.minyust.MinyustFamilyBatchRequestProcessor;
import uz.fido.pfexchange.utils.MinyustFamilyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class MinyustFamilyBatchRequestProcessorImpl implements MinyustFamilyBatchRequestProcessor {

    private final MinyustBatchProperties properties;

    private final WomenRepository repository;
    private final CustomQueryRepository customQueryRepository;
    private final WomenChildrenInfRepository childrenRepository;
    // private final ExchangeMinyustFamilyInfRepository minyustFamilyInfRepository;

    private volatile RateLimiter rateLimiter;
    private static final AtomicInteger consecutiveTimeouts = new AtomicInteger(0);
    private static final AtomicInteger consecutiveSuccesses = new AtomicInteger(0);
    private final AtomicInteger batchCounter = new AtomicInteger(0);

    private final ExecutorService executorService;
    private final RestClient restClient;

    private enum CircuitState {
        CLOSED,
        OPEN,
        HALF_OPEN,
    }

    private static volatile CircuitState circuitState = CircuitState.CLOSED;
    private static volatile LocalDateTime circuitOpenedAt;

    private static final int LARGE_DATA_THRESHOLD = 10_000;
    private static final Integer DEFAULT_GENDER = 0;
    private static final Long DEFAULT_ID = 0L;
    private static final String CHILD_BIRTH_ACTIVITY_CODE = "07";
    private static final String URL = "http://10.190.24.138:96/api/ZagsToMinFin/GetFamily";

    // Processing lock to prevent concurrent batch executions
    private final AtomicInteger processingLock = new AtomicInteger(0);

    public MinyustFamilyBatchRequestProcessorImpl(
            WomenRepository repository,
            CustomQueryRepository customQueryRepository,
            WomenChildrenInfRepository childrenRepository,
            // ExchangeMinyustFamilyInfRepository minyustFamilyInfRepository,
            MinyustBatchProperties properties,
            RestClient restClient) {
        this.repository = repository;
        this.customQueryRepository = customQueryRepository;
        this.childrenRepository = childrenRepository;
        // this.minyustFamilyInfRepository = minyustFamilyInfRepository;
        this.properties = properties;

        this.rateLimiter = RateLimiter.create(properties.getRateLimitPerSecond());
        this.executorService = createManagedExecutor();
        this.restClient = restClient;

        log.info(
                "MinyustFamilyBatchProcessor initialized - Rate: {} req/s, Threads: {}, Batch: {}",
                properties.getRateLimitPerSecond(),
                properties.getThreadPoolSize(),
                properties.getBatchSize());
    }

    private ExecutorService createManagedExecutor() {
        return new ThreadPoolExecutor(
                properties.getThreadPoolSize(),
                properties.getThreadPoolSize(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t =
                                new Thread(r, "minyust-worker-" + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down MinyustBatchProcessor...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("Forcing executor shutdown");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public ProcessingResult processOneBatch() {
        // Circuit breaker check
        if (circuitState == CircuitState.OPEN) {
            if (shouldAttemptRecovery()) {
                circuitState = CircuitState.HALF_OPEN;
                log.info("Circuit breaker entering HALF_OPEN state");
            } else {
                long secondsRemaining =
                        java.time.Duration.between(
                                        LocalDateTime.now(),
                                        circuitOpenedAt.plusNanos(
                                                properties.getCircuitBreaker().getOpenDurationMs()
                                                        * 1_000_000))
                                .getSeconds();

                log.warn(
                        "Circuit breaker is OPEN. Pausing processing. Retry in {} seconds",
                        secondsRemaining);
                return ProcessingResult.failure("Circuit breaker OPEN - API unavailable");
            }
        }

        // Acquire processing lock
        if (!processingLock.compareAndSet(0, 1)) {
            log.warn("Another batch is currently processing, skipping this run");
            return ProcessingResult.failure("Another batch in progress");
        }

        try {
            int currentBatch = batchCounter.incrementAndGet();
            log.info("======== Starting Batch #{} ========", currentBatch);

            // Fetch batch
            List<Women> batch =
                    repository.findByStatusWithLimit(
                            MinyustFamilyStatus.READY.name(), properties.getBatchSize());

            if (batch.isEmpty()) {
                log.info("No READY records found. Processing complete!");
                return ProcessingResult.success("No records to process");
            }

            log.info("Batch #{}: Processing {} records", currentBatch, batch.size());

            // Process batch in parallel
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            AtomicInteger differentCount = new AtomicInteger(0);

            List<CompletableFuture<ProcessingResult>> futures =
                    batch.stream()
                            .map(
                                    request ->
                                            CompletableFuture.supplyAsync(
                                                    () -> {
                                                        try {
                                                            ProcessingResult result =
                                                                    processRequest(request);
                                                            if (result.isSuccess()) {
                                                                successCount.incrementAndGet();
                                                                if (MinyustFamilyStatus.DIFFERENT
                                                                        .name()
                                                                        .equals(
                                                                                request.getStatus()
                                                                                        .name())) {
                                                                    differentCount
                                                                            .incrementAndGet();
                                                                }
                                                            } else {
                                                                failureCount.incrementAndGet();
                                                            }
                                                            return result;
                                                        } catch (Exception e) {
                                                            log.error(
                                                                    "Unhandled exception processing"
                                                                            + " request {}",
                                                                    request.getId(),
                                                                    e);
                                                            failureCount.incrementAndGet();
                                                            return ProcessingResult.failure(
                                                                    "Unhandled exception");
                                                        }
                                                    },
                                                    executorService))
                            .toList();

            // Wait for batch completion with graceful error handling
            try {
                CompletableFuture<Void> allOf =
                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                .exceptionally(
                                        throwable -> {
                                            log.error(
                                                    "Some futures failed in batch {}",
                                                    currentBatch,
                                                    throwable);
                                            return null; // Continue processing
                                        });

                allOf.get(properties.getBatchTimeoutMinutes(), TimeUnit.MINUTES);

                log.info(
                        "Batch #{} completed: Success={}, Failed={}, Different={}, Total={}",
                        currentBatch,
                        successCount.get(),
                        failureCount.get(),
                        differentCount.get(),
                        batch.size());

                logProgress();

                // Consider batch successful if at least some records processed
                if (successCount.get() > 0) {
                    return ProcessingResult.success(
                            String.format(
                                    "Processed %d/%d records", successCount.get(), batch.size()));
                } else if (failureCount.get() == batch.size()) {
                    return ProcessingResult.failure("All records in batch failed");
                } else {
                    return ProcessingResult.success("Batch completed with some failures");
                }
            } catch (TimeoutException e) {
                log.error(
                        "Batch #{} timed out after {} minutes. Success: {}, Failed: {}",
                        currentBatch,
                        properties.getBatchTimeoutMinutes(),
                        successCount.get(),
                        failureCount.get());
                return ProcessingResult.failure("Batch timeout");
            } catch (Exception e) {
                log.error("Batch #{} failed with exception", currentBatch, e);
                return ProcessingResult.failure("Batch exception: " + e.getMessage());
            }
        } finally {
            processingLock.set(0);
            log.info("======== Batch Completed ========");
        }
    }

    @Override
    public void processAllPendingRequests() {
        log.info("Starting continuous batch processing (manual mode)...");

        int totalProcessed = 0;
        int batchNumber = 0;
        int consecutiveFailures = 0;
        final int MAX_CONSECUTIVE_FAILURES = 3;

        while (true) {
            batchNumber++;

            // Recover stuck records before each batch
            int recovered = recoverStuckRecords();
            if (recovered > 0) {
                log.info("Recovered {} stuck records before batch {}", recovered, batchNumber);
            }

            ProcessingResult result = processOneBatch();

            if (result.getMessage() != null && result.getMessage().contains("No records")) {
                log.info("All records processed. Total batches: {}", batchNumber);
                break;
            }

            if (!result.isSuccess()) {
                consecutiveFailures++;
                log.warn(
                        "Batch {} failed: {}. Consecutive failures: {}/{}",
                        batchNumber,
                        result.getMessage(),
                        consecutiveFailures,
                        MAX_CONSECUTIVE_FAILURES);

                // Only stop after multiple consecutive failures
                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    log.error(
                            "Stopping after {} consecutive batch failures. Check circuit breaker"
                                    + " and API availability.",
                            consecutiveFailures);
                    break;
                }

                // Wait longer between failed batches
                try {
                    log.info("Waiting 30 seconds before retry...");
                    Thread.sleep(30_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Processing interrupted during failure backoff");
                    break;
                }
            } else {
                consecutiveFailures = 0; // Reset on success
                totalProcessed += properties.getBatchSize();

                // Normal delay between successful batches
                try {
                    Thread.sleep(5_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Processing interrupted");
                    break;
                }
            }
        }

        log.info(
                "Continuous processing completed. Processed {} batches, total records: ~{}",
                batchNumber,
                totalProcessed);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recoverStuckRecords() {
        try {
            // Reduce threshold to 5 minutes for faster recovery
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);

            int recovered =
                    repository.resetStuckRecords(
                            MinyustFamilyStatus.PROCESSING,
                            MinyustFamilyStatus.READY,
                            threshold,
                            properties.getMaxRetries());

            if (recovered > 0) {
                log.warn("Recovered {} stuck PROCESSING records (older than 5 minutes)", recovered);
            }

            return recovered;
        } catch (Exception e) {
            log.error("Error recovering stuck records", e);
            return 0;
        }
    }

    private ProcessingResult processRequest(Women request) {
        int maxRetries = properties.getMaxRetries();
        int attempt = 0;
        Exception lastException = null;

        if (circuitState == CircuitState.OPEN) {
            if (shouldAttemptRecovery()) {
                circuitState = CircuitState.HALF_OPEN;
                log.info("Circuit breaker entering HALF_OPEN state");
            } else {
                log.debug("Circuit OPEN, skipping request {}", request.getId());
                // Reset to READY so it can be retried later
                safeUpdateStatus(request, MinyustFamilyStatus.READY);
                return ProcessingResult.failure("Circuit breaker OPEN");
            }
        }

        // Update to PROCESSING
        safeUpdateStatus(request, MinyustFamilyStatus.PROCESSING);

        while (attempt < maxRetries) {
            try {
                rateLimiter.acquire();

                MinyustFamilyResponseDto response =
                        callExternalService(
                                MinyustFamilyRequestDto.builder()
                                        .ID(String.valueOf(request.getId()))
                                        .pnfl(request.getPinpp())
                                        .tin("20201210")
                                        .build());

                handleSuccess();
                request.setRequestDate(LocalDateTime.now());

                if ("1".equals(response.getResult_code())) {
                    // Store FULL response - CLOB can handle it!
                    String responseData = response.toString();
                    request.setDataIn(responseData);

                    // Log if data is unusually large (for monitoring)
                    if (responseData.length() > LARGE_DATA_THRESHOLD) {
                        log.info(
                                "Large response for request {}: {} bytes, {} children",
                                request.getId(),
                                responseData.length(),
                                response.getItems() != null ? response.getItems().size() : 0);
                    }

                    if (response.getItems() != null && !response.getItems().isEmpty()) {
                        processChildren(response, request);
                        determineFinalStatus(response, request);
                    } else {
                        request.setStatus(MinyustFamilyStatus.COMPLETED);
                    }
                } else {
                    request.setStatus(MinyustFamilyStatus.FAILED);
                    request.setDataErr(
                            "Result code: "
                                    + response.getResult_code()
                                    + ", Message: "
                                    + response.getResult_message());
                }

                safeUpdateStatus(request, request.getStatus());
                return ProcessingResult.success();
            } catch (ResourceAccessException e) {
                lastException = e;
                attempt++;

                if (isTimeoutException(e)) {
                    handleTimeout();

                    if (attempt < maxRetries) {
                        long backoffMs = calculateBackoff(attempt);
                        try {
                            Thread.sleep(backoffMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            request.setStatus(MinyustFamilyStatus.FAILED);
                            request.setDataErr("Interrupted during retry backoff");
                            safeUpdateStatus(request, MinyustFamilyStatus.FAILED);
                            return ProcessingResult.failure("Interrupted");
                        }
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                log.error("Unexpected error processing request {}", request.getId(), e);
                lastException = e;
                break;
            }
        }

        // Max retries exceeded or non-recoverable error
        request.setStatus(MinyustFamilyStatus.FAILED);
        request.setDataErr(
                "Max retries exceeded: "
                        + (lastException != null ? lastException.getMessage() : "unknown"));
        safeUpdateStatus(request, MinyustFamilyStatus.FAILED);
        return ProcessingResult.failure("Max retries exceeded");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Women request, MinyustFamilyStatus status) {
        try {
            request.setStatus(status);

            // Log large CLOB operations for monitoring
            if (request.getDataIn() != null
                    && request.getDataIn().length() > LARGE_DATA_THRESHOLD) {
                log.debug(
                        "Saving large CLOB for request {}: data_in={} bytes",
                        request.getId(),
                        request.getDataIn().length());
            }

            Women saved = repository.saveAndFlush(request);
            log.debug(
                    "Status updated: {} -> {} (ID: {})",
                    request.getId(),
                    status,
                    saved.getStatus());
        } catch (Exception e) {
            log.error(
                    "CRITICAL: Failed to save status {} for request {}: {}",
                    status,
                    request.getId(),
                    e.getMessage(),
                    e);

            try {
                log.warn("Attempting fallback save for request {}", request.getId());

                String originalDataIn = request.getDataIn();

                request.setDataIn(null);
                request.setDataErr(
                        "Original save failed: "
                                + (e.getMessage() != null
                                        ? e.getMessage()
                                                .substring(
                                                        0, Math.min(500, e.getMessage().length()))
                                        : "unknown")
                                + ". Original data_in size: "
                                + (originalDataIn != null ? originalDataIn.length() : 0)
                                + " bytes");
                request.setStatus(MinyustFamilyStatus.FAILED);

                repository.saveAndFlush(request);
                log.warn("Fallback save succeeded for request {}", request.getId());
            } catch (Exception fallbackError) {
                log.error(
                        "Fallback save also failed for request {}", request.getId(), fallbackError);
                throw fallbackError;
            }
        }
    }

    private void safeUpdateStatus(Women request, MinyustFamilyStatus status) {
        try {
            updateStatus(request, status);
        } catch (Exception e) {
            log.error(
                    "Failed to update status for request {} to {}. Marking as FAILED in memory"
                            + " only.",
                    request.getId(),
                    status,
                    e);
            request.setStatus(MinyustFamilyStatus.FAILED);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processChildren(MinyustFamilyResponseDto response, Women pfWomen) {
        try {
            for (MinyustFamilyItemDto item : response.getItems()) {
                WomenChildrenInf childRecord = mapToEntity(item, pfWomen);
                childrenRepository.save(childRecord);
            }
        } catch (Exception e) {
            log.error("Error saving children for request {}", pfWomen.getId(), e);
            throw e;
        }
    }

    private void determineFinalStatus(MinyustFamilyResponseDto response, Women request) {
        List<PersonActivityDto> womanActivities =
                customQueryRepository.getPersonActivities(
                        request.getPersonId(), request.getApplicationId());

        if (womanActivities.isEmpty()) {
            request.setStatus(MinyustFamilyStatus.COMPLETED);
            return;
        }

        List<PersonActivityDto> stuffActivities =
                womanActivities.stream().filter(a -> a.stuffFlag().equals("N")).toList();
        List<PersonActivityDto> workStuffActivities =
                womanActivities.stream()
                        .filter(activity -> !CHILD_BIRTH_ACTIVITY_CODE.equals(activity.code()))
                        .toList();

        List<PersonActivityDto> originalChildBirthActivities =
                stuffActivities.stream()
                        .filter(activity -> CHILD_BIRTH_ACTIVITY_CODE.equals(activity.code()))
                        .toList();

        List<PersonActivityDto> transformedChildBirthActivities =
                originalChildBirthActivities.stream()
                        .map(
                                activity ->
                                        new PersonActivityDto(
                                                activity.beginDate().minusDays(126),
                                                activity.endDate(),
                                                activity.code(),
                                                null))
                        .toList();

        List<PersonActivityDto> allActivitiesForFiltering = new ArrayList<>();

        List<PersonActivityDto> womanWorkActivitiesOnly =
                stuffActivities.stream()
                        .filter(activity -> !CHILD_BIRTH_ACTIVITY_CODE.equals(activity.code()))
                        .toList();

        allActivitiesForFiltering.addAll(womanWorkActivitiesOnly);
        allActivitiesForFiltering.addAll(transformedChildBirthActivities);

        List<MinyustFamilyItemDto> childrenInActivityPeriod =
                response.getItems().stream()
                        .filter(
                                member ->
                                        request.getPinpp().equals(member.getM_pnfl())
                                                && member.getBirth_date() != null)
                        .filter(
                                member -> {
                                    LocalDate birthDate = member.getBirth_date();
                                    return allActivitiesForFiltering.stream()
                                            .anyMatch(
                                                    activity ->
                                                            !birthDate.isBefore(
                                                                            activity.beginDate())
                                                                    && !birthDate.isAfter(
                                                                            activity.endDate()));
                                })
                        .toList();

        List<MinyustFamilyItemDto> children =
                response.getItems().stream()
                        .filter(
                                member ->
                                        request.getPinpp().equals(member.getM_pnfl())
                                                && member.getBirth_date() != null)
                        .toList();

        if (!childrenInActivityPeriod.isEmpty() && transformedChildBirthActivities.isEmpty()) {
            request.setStatus(MinyustFamilyStatus.DIFFERENT);
            return;
        }

        if (childrenInActivityPeriod.isEmpty() && !transformedChildBirthActivities.isEmpty()) {
            request.setStatus(MinyustFamilyStatus.DIFFERENT);
            return;
        }

        ValidationResult validationResult =
                validateChildrenAgainstActivities(
                        childrenInActivityPeriod,
                        children,
                        transformedChildBirthActivities,
                        workStuffActivities);

        if (validationResult.allChildrenMatched()
                && validationResult.allActivitiesCovered()
                && !validationResult.isWorkActivitiesAffected()) {
            request.setStatus(MinyustFamilyStatus.COMPLETED);
        } else {
            request.setStatus(MinyustFamilyStatus.DIFFERENT);
        }
    }

    private ValidationResult validateChildrenAgainstActivities(
            List<MinyustFamilyItemDto> childrenInActivityPeriod,
            List<MinyustFamilyItemDto> children,
            List<PersonActivityDto> childBirthActivities,
            List<PersonActivityDto> womanWorkActivitiesOnly) {
        Set<Integer> activitiesWithChildren = new HashSet<>();
        List<MinyustFamilyItemDto> unmatchedChildren = new ArrayList<>();
        boolean isWorkActivitiesAffected = false;

        for (MinyustFamilyItemDto child : children) {

            LocalDate birthDate = child.getBirth_date();

            for (int i = 0; i < womanWorkActivitiesOnly.size(); i++) {
                PersonActivityDto activity = womanWorkActivitiesOnly.get(i);

                if (birthDate.isAfter(activity.beginDate().minusDays(80))) {
                    isWorkActivitiesAffected = true;
                    break;
                }
            }

            if (isWorkActivitiesAffected) {
                break;
            }
        }

        if (!isWorkActivitiesAffected) {

            for (MinyustFamilyItemDto child : childrenInActivityPeriod) {
                LocalDate birthDate = child.getBirth_date();
                boolean childMatchedChildBirthActivity = false;

                for (int i = 0; i < childBirthActivities.size(); i++) {
                    PersonActivityDto activity = childBirthActivities.get(i);

                    if (!birthDate.isBefore(activity.beginDate())
                            && !birthDate.isAfter(activity.endDate())) {
                        childMatchedChildBirthActivity = true;
                        activitiesWithChildren.add(i);
                    }
                }

                if (!childMatchedChildBirthActivity) {
                    unmatchedChildren.add(child);
                }
            }
        }

        boolean allChildrenMatched = unmatchedChildren.isEmpty();
        boolean allActivitiesCovered = activitiesWithChildren.size() == childBirthActivities.size();

        return new ValidationResult(
                allChildrenMatched,
                allActivitiesCovered,
                isWorkActivitiesAffected,
                unmatchedChildren,
                activitiesWithChildren.size(),
                childBirthActivities.size());
    }

    private void logProgress() {
        try {
            Map<String, Long> statusCounts = repository.getStatusCounts();

            long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
            long processed = total - statusCounts.getOrDefault("READY", 0L);
            double percentage = total > 0 ? ((processed * 100.0) / total) : 0;

            log.info(
                    "Overall Progress: {}/{} ({}%) - READY: {}, PROCESSING: {}, COMPLETED: {},"
                            + " DIFFERENT: {}, FAILED: {}",
                    processed,
                    total,
                    String.format("%.2f", percentage),
                    statusCounts.getOrDefault("READY", 0L),
                    statusCounts.getOrDefault("PROCESSING", 0L),
                    statusCounts.getOrDefault("COMPLETED", 0L),
                    statusCounts.getOrDefault("DIFFERENT", 0L),
                    statusCounts.getOrDefault("FAILED", 0L));
        } catch (Exception e) {
            log.warn("Could not log progress", e);
        }
    }

    @Override
    public long getPendingCount() {
        return repository.countUnprocessed();
    }

    @Override
    public Map<String, Long> getStatusSummary() {
        return repository.getStatusCounts();
    }

    public CircuitState getCircuitState() {
        return circuitState;
    }

    public double getCurrentRateLimit() {
        return rateLimiter.getRate();
    }

    private void handleTimeout() {
        int timeouts = consecutiveTimeouts.incrementAndGet();
        consecutiveSuccesses.set(0);
        double currentRate = rateLimiter.getRate();

        if (timeouts >= properties.getCircuitBreaker().getFailureThreshold()) {
            circuitState = CircuitState.OPEN;
            circuitOpenedAt = LocalDateTime.now();
            log.error("Circuit breaker OPENED due to {} consecutive timeouts", timeouts);
        }

        if (timeouts % 3 == 0 && currentRate > 5.0) {
            double newRate = Math.max(5.0, currentRate * 0.5);
            rateLimiter = RateLimiter.create(newRate);
            log.warn(
                    "Rate limit reduced: {} -> {} req/s",
                    String.format("%.2f", currentRate),
                    String.format("%.2f", newRate));
        }
    }

    private void handleSuccess() {
        consecutiveTimeouts.set(0);
        int successes = consecutiveSuccesses.incrementAndGet();

        if (circuitState == CircuitState.HALF_OPEN
                && successes >= properties.getCircuitBreaker().getHalfOpenSuccessThreshold()) {
            circuitState = CircuitState.CLOSED;
            log.info("Circuit breaker CLOSED after successful recovery");
        }

        double currentRate = rateLimiter.getRate();
        if (successes % 50 == 0 && currentRate < properties.getRateLimitPerSecond()) {
            double newRate = Math.min(properties.getRateLimitPerSecond(), currentRate * 1.2);
            rateLimiter = RateLimiter.create(newRate);
            log.info(
                    "Rate limit increased: {} -> {} req/s",
                    String.format("%.2f", currentRate),
                    String.format("%.2f", newRate));
        }
    }

    private boolean shouldAttemptRecovery() {
        if (circuitOpenedAt == null) return false;
        return LocalDateTime.now()
                .isAfter(
                        circuitOpenedAt.plusNanos(
                                properties.getCircuitBreaker().getOpenDurationMs() * 1_000_000));
    }

    private long calculateBackoff(int attempt) {
        long baseDelay = 1000L;
        long exponentialDelay = baseDelay * (long) Math.pow(2, attempt - 1);
        long jitter = (long) (exponentialDelay * 0.25 * Math.random());
        return Math.min(exponentialDelay + jitter, 30_000L);
    }

    private boolean isTimeoutException(Exception e) {
        return (e instanceof ResourceAccessException
                && (e.getCause() instanceof java.net.SocketTimeoutException
                        || e.getMessage().contains("timeout")
                        || e.getMessage().contains("timed out")));
    }

    private MinyustFamilyResponseDto callExternalService(MinyustFamilyRequestDto requestDto) {
        try {
            return restClient
                    .post()
                    .uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestDto)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 429,
                            (request, response) -> {
                                log.warn("Rate limit (429) from server");
                                handleTimeout();
                                throw new ResourceAccessException("Rate limit exceeded");
                            })
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (request, response) -> {
                                throw new IllegalArgumentException(
                                        "Client error: " + response.getStatusCode());
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new IllegalStateException(
                                        "Server error: " + response.getStatusCode());
                            })
                    .body(MinyustFamilyResponseDto.class);
        } catch (ResourceAccessException e) {
            throw e;
        }
    }

    private WomenChildrenInf mapToEntity(MinyustFamilyItemDto dto, Women pfWomen) {
        return WomenChildrenInf.builder()
                .women(pfWomen)
                .pinpp(dto.getPnfl())
                .surname(dto.getSurname())
                .name(dto.getName())
                .patronym(dto.getPatronym())
                .birthDate(dto.getBirth_date())
                .gender(parseGender(dto.getGender_code()))
                .regNumber(dto.getDoc_num())
                .regDate((dto.getDoc_date()))
                .recvZagsId(parseLong(dto.getBranch()))
                .certificateSeria(dto.getCert_series())
                .certificateNumber(dto.getCert_number())
                .certificateDate(dto.getCert_birth_date())
                .fatherPin(dto.getF_pnfl())
                .fatherSurnameLatin(dto.getF_family())
                .fatherNameLatin(dto.getF_first_name())
                .fatherPatronymLatin(dto.getF_patronym())
                .fatherBirthDate(dto.getF_birth_day())
                .motherPin(dto.getM_pnfl())
                .motherSurnameLatin(dto.getM_family())
                .motherNameLatin(dto.getM_first_name())
                .motherPatronymLatin(dto.getM_patronym())
                .motherBirthDate(dto.getM_birth_day())
                .createdBy(1L)
                .creationDate(LocalDate.now())
                .isActive("Y")
                .isAlive(dto.getLive_status())
                .build();
    }

    private Integer parseGender(String genderCode) {
        if (genderCode == null || genderCode.isEmpty()) return DEFAULT_GENDER;
        try {
            return Integer.parseInt(genderCode.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_GENDER;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) return DEFAULT_ID;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_ID;
        }
    }

    private record ValidationResult(
            boolean allChildrenMatched,
            boolean allActivitiesCovered,
            boolean isWorkActivitiesAffected,
            List<MinyustFamilyItemDto> unmatchedChildren,
            int coveredActivitiesCount,
            int totalActivitiesCount) {}
}
