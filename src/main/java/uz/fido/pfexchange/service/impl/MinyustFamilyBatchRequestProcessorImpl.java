package uz.fido.pfexchange.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import uz.fido.pfexchange.dto.minyust.MinyustFamilyItemDto;
import uz.fido.pfexchange.dto.minyust.MinyustFamilyRequestDto;
import uz.fido.pfexchange.dto.minyust.MinyustFamilyResponseDto;
import uz.fido.pfexchange.dto.minyust.ProcessingResult;
import uz.fido.pfexchange.entity.minyust.PfWomen;
import uz.fido.pfexchange.entity.minyust.PfWomenChildrenInf;
import uz.fido.pfexchange.repository.CustomQueryRepository;
import uz.fido.pfexchange.repository.minyust.PfExchangeMinyustFamilyInfRepository;
import uz.fido.pfexchange.repository.minyust.PfWomenChildrenInfRepository;
import uz.fido.pfexchange.repository.minyust.PfWomenRepository;
import uz.fido.pfexchange.service.MinyustFamilyBatchRequestProcessor;
import uz.fido.pfexchange.utils.MinyustFamilyStatus;
import uz.fido.pfexchange.utils.Utils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinyustFamilyBatchRequestProcessorImpl
    implements MinyustFamilyBatchRequestProcessor {

    private final PfWomenRepository repository;
    private final CustomQueryRepository customQueryRepository;

    private volatile RateLimiter rateLimiter = RateLimiter.create(50.0);
    private final AtomicInteger consecutiveTimeouts = new AtomicInteger(0);
    private final AtomicInteger consecutiveSuccesses = new AtomicInteger(0);
    private static final double MIN_RATE = 5.0;
    private static final double MAX_RATE = 50.0;
    private static final double BACKOFF_MULTIPLIER = 0.5; // Reduce by 50%
    private static final double RECOVERY_MULTIPLIER = 1.2; // Increase by 20%

    private final ExecutorService executorService = createExecutor();
    private final PfWomenChildrenInfRepository childrenRepository;
    private final PfExchangeMinyustFamilyInfRepository minyustFamilyInfRepository;
    private final RestClient restClient = createRestClient();

    private enum CircuitState {
        CLOSED,
        OPEN,
        HALF_OPEN,
    }

    private volatile CircuitState circuitState = CircuitState.CLOSED;
    private volatile LocalDateTime circuitOpenedAt;
    private static final long CIRCUIT_OPEN_DURATION_MS = 60_000; // 1 minute
    private static final String PAY_STATUS_CODE = "05";
    private static final Integer DEFAULT_GENDER = 0;
    private static final Long DEFAULT_ID = 0L;
    private static final LocalDate DEFAULT_DATE = LocalDate.of(1900, 1, 1);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String URL =
        "http://10.190.24.138:96/api/ZagsToMinFin/GetFamily";

    private ExecutorService createExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("minyust-batch-");
        executor.initialize();
        return executor.getThreadPoolExecutor();
    }

    @Override
    public void processAllPendingRequests() {
        log.info("Starting parallel batch processing...");

        int batchSize = 1000;
        int totalProcessed = 0;
        int totalSuccess = 0;
        int totalFailed = 0;

        AtomicInteger idCounter = new AtomicInteger(1);

        while (true) {
            List<PfWomen> batch = repository.findByStatusWithLimit(
                MinyustFamilyStatus.READY.name(),
                batchSize
            );

            if (batch.isEmpty()) {
                log.info("No more READY records to process");
                break;
            }

            log.info(
                "Processing batch of {} records in parallel",
                batch.size()
            );

            List<CompletableFuture<ProcessingResult>> futures = batch
                .stream()
                .map(request ->
                    CompletableFuture.supplyAsync(
                        () ->
                            processRequest(
                                request,
                                idCounter.getAndIncrement()
                            ),
                        executorService
                    )
                )
                .toList();

            CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            ).join();

            for (CompletableFuture<ProcessingResult> future : futures) {
                try {
                    ProcessingResult result = future.get();
                    if (result.isSuccess()) {
                        totalSuccess++;
                    } else {
                        totalFailed++;
                    }
                    totalProcessed++;
                } catch (Exception e) {
                    log.error("Error getting result", e);
                    totalFailed++;
                    totalProcessed++;
                }
            }

            if (totalProcessed % 100 == 0) {
                log.info(
                    "Progress: {} processed (Success: {}, Failed: {})",
                    totalProcessed,
                    totalSuccess,
                    totalFailed
                );
            }
        }

        log.info(
            "Batch processing completed. Total: {}, Success: {}, Failed: {}",
            totalProcessed,
            totalSuccess,
            totalFailed
        );
    }

    private ProcessingResult processRequest(PfWomen request, int id) {
        List<String> currentExchangeChildren;
        List<String> currentSavedChildren;

        if (circuitState == CircuitState.OPEN) {
            if (shouldAttemptRecovery()) {
                circuitState = CircuitState.HALF_OPEN;
                log.info("Circuit breaker entering HALF_OPEN state");
            } else {
                return ProcessingResult.failure("Circuit breaker is OPEN");
            }
        }

        int maxRetries = 3;
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                request.setStatus(MinyustFamilyStatus.PROCESSING);
                repository.save(request);

                rateLimiter.acquire();

                MinyustFamilyResponseDto response =
                    callExternalServiceWithRetry(
                        MinyustFamilyRequestDto.builder()
                            .ID(String.valueOf(id))
                            .pnfl(request.getPinpp())
                            .tin("20201210")
                            .build(),
                        attempt
                    );

                // Success - handle rate limiter recovery
                handleSuccess();

                if ("1".equals(response.getResult_code())) {
                    if (
                        response.getItems() != null &&
                        !response.getItems().isEmpty()
                    ) {
                        for (MinyustFamilyItemDto item : response.getItems()) {
                            PfWomenChildrenInf childRecord = mapToEntity(
                                item,
                                request
                            );
                            childrenRepository.save(childRecord);
                        }
                    }

                    request.setStatus(MinyustFamilyStatus.COMPLETED);
                    request.setRequestDate(LocalDateTime.now());
                    request.setDataIn(response.toString());

                    if (
                        response.getItems() != null &&
                        !response.getItems().isEmpty()
                    ) {
                        currentExchangeChildren = response
                            .getItems()
                            .stream()
                            .filter(
                                member ->
                                    member.getM_pnfl() != null &&
                                    member
                                        .getM_pnfl()
                                        .equals(request.getPinpp())
                            )
                            .map(MinyustFamilyItemDto::getPnfl)
                            .toList();
                        currentSavedChildren = minyustFamilyInfRepository
                            .getLatestMinyustByPinpp(request.getPinpp())
                            .orElse(null);

                        if (currentSavedChildren == null) {
                            request.setStatus(MinyustFamilyStatus.NOT_FOUND);
                        } else if (
                            currentExchangeChildren.size() !=
                                currentSavedChildren.size() ||
                            !new HashSet<>(currentExchangeChildren).containsAll(
                                currentSavedChildren
                            )
                        ) {
                            request.setStatus(MinyustFamilyStatus.DIFFERENT);
                            // customQueryRepository.updateApplicationPayStatusCode(
                            //     request.getApplicationId(),
                            //     PAY_STATUS_CODE
                            // );
                        }
                    }
                } else {
                    request.setStatus(MinyustFamilyStatus.FAILED);
                    request.setRequestDate(LocalDateTime.now());
                    request.setDataErr(
                        "Result code: " +
                            response.getResult_code() +
                            ", Message: " +
                            response.getResult_message()
                    );
                }

                return ProcessingResult.success();
            } catch (ResourceAccessException e) {
                lastException = e;
                attempt++;

                if (isTimeoutException(e)) {
                    handleTimeout();

                    if (attempt < maxRetries) {
                        long backoffMs = calculateBackoff(attempt);
                        log.warn(
                            "Timeout on attempt {}/{} for request {}. Backing off for {}ms",
                            attempt,
                            maxRetries,
                            request.getId(),
                            backoffMs
                        );
                        try {
                            Thread.sleep(backoffMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("Thread interrupted during backoff", ie);
                            request.setStatus(MinyustFamilyStatus.FAILED);
                            request.setDataErr("Processing interrupted");
                            repository.save(request);
                            return ProcessingResult.failure(
                                "Thread interrupted"
                            );
                        }
                    }
                } else {
                    // Non-timeout error, don't retry
                    break;
                }
            } catch (Exception e) {
                log.error(
                    "Failed to process request {}: {}",
                    request.getId(),
                    e.getMessage()
                );
                request.setStatus(MinyustFamilyStatus.FAILED);
                request.setDataErr(e.getMessage());
                repository.save(request);
                return ProcessingResult.failure(e.getMessage());
            }
        }
        request.setStatus(MinyustFamilyStatus.FAILED);
        request.setDataErr(
            "Max retries exceeded: " + lastException.getMessage()
        );
        repository.save(request);
        return ProcessingResult.failure("Max retries exceeded");
    }

    @Override
    public long getPendingCount() {
        return repository.countUnprocessed();
    }

    private MinyustFamilyResponseDto callExternalService(
        MinyustFamilyRequestDto requestDto
    ) {
        try {
            log.info(
                "Calling external service: {} with body: {}",
                URL,
                requestDto
            );

            return restClient
                .post()
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve()
                .onStatus(
                    HttpStatusCode::is4xxClientError,
                    (request, response) -> {
                        log.error("4xx error: {}", response.getStatusCode());
                        throw new IllegalArgumentException(
                            "Client error: " + response.getStatusCode()
                        );
                    }
                )
                .onStatus(
                    HttpStatusCode::is5xxServerError,
                    (request, response) -> {
                        log.error("5xx error: {}", response.getStatusCode());
                        throw new IllegalStateException(
                            "Server error: " + response.getStatusCode()
                        );
                    }
                )
                .body(MinyustFamilyResponseDto.class);
        } catch (ResourceAccessException e) {
            log.error("I/O error connecting to {}: {}", URL, e.getMessage(), e);
            throw new RuntimeException(
                "Failed to connect to external service",
                e
            );
        }
    }

    private PfWomenChildrenInf mapToEntity(
        MinyustFamilyItemDto dto,
        PfWomen pfWomen
    ) {
        return PfWomenChildrenInf.builder()
            .pfWomen(pfWomen)
            .pinpp(dto.getPnfl())
            .surname(dto.getSurname())
            .name(dto.getName())
            .patronym(dto.getPatronym())
            .birthDate(parseDate(dto.getBirth_date()))
            .gender(parseGender(dto.getGender_code()))
            .regNumber(dto.getDoc_num())
            .regDate(parseDate(dto.getDoc_date()))
            .recvZagsId(parseLong(dto.getBranch()))
            .certificateSeria(dto.getCert_series())
            .certificateNumber(dto.getCert_number())
            .certificateDate(parseDate(dto.getCert_birth_date()))
            .fatherPin(dto.getF_pnfl())
            .fatherSurnameLatin(dto.getF_family())
            .fatherNameLatin(dto.getF_first_name())
            .fatherPatronymLatin(dto.getF_patronym())
            .fatherBirthDate(parseDate(dto.getF_birth_day()))
            .motherPin(dto.getM_pnfl())
            .motherSurnameLatin(dto.getM_family())
            .motherNameLatin(dto.getM_first_name())
            .motherPatronymLatin(dto.getM_patronym())
            .motherBirthDate(parseDate(dto.getM_birth_day()))
            .createdBy(1L)
            .creationDate(LocalDate.now())
            .isActive("Y")
            .isAlive(dto.getLive_status())
            .build();
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return DEFAULT_DATE;
        }

        try {
            String normalizedDateString = Utils.normalizeDateString(dateString);
            return LocalDate.parse(normalizedDateString, formatter);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            log.error("Failed to parse date string: {}", dateString, e);
            return DEFAULT_DATE;
        }
    }

    private Integer parseGender(String genderCode) {
        if (genderCode == null || genderCode.isEmpty()) {
            return DEFAULT_GENDER;
        }

        try {
            return Integer.parseInt(genderCode.trim());
        } catch (NumberFormatException e) {
            log.error("Failed to parse gender code: {}", genderCode, e);
            return DEFAULT_GENDER;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return DEFAULT_ID;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.error("Failed to parse long value: {}", value, e);
            return DEFAULT_ID;
        }
    }

    private void handleTimeout() {
        int timeouts = consecutiveTimeouts.incrementAndGet();
        consecutiveSuccesses.set(0);

        double currentRate = rateLimiter.getRate();

        // Open circuit breaker if too many consecutive timeouts
        if (timeouts >= 10) {
            circuitState = CircuitState.OPEN;
            circuitOpenedAt = LocalDateTime.now();
            log.error(
                "Circuit breaker OPENED due to {} consecutive timeouts. " +
                    "Pausing for {} seconds",
                timeouts,
                CIRCUIT_OPEN_DURATION_MS / 1000
            );
        }

        // Reduce rate on every 3 consecutive timeouts
        if (timeouts % 3 == 0 && currentRate > MIN_RATE) {
            double newRate = Math.max(
                MIN_RATE,
                currentRate * BACKOFF_MULTIPLIER
            );
            rateLimiter = RateLimiter.create(newRate);

            log.warn(
                "Rate limit reduced: {} -> {} req/s (consecutive timeouts: {})",
                String.format("%.2f", currentRate),
                String.format("%.2f", newRate),
                timeouts
            );
        }
    }

    private void handleSuccess() {
        consecutiveTimeouts.set(0);
        int successes = consecutiveSuccesses.incrementAndGet();

        // Close circuit breaker if in HALF_OPEN and getting successes
        if (circuitState == CircuitState.HALF_OPEN && successes >= 5) {
            circuitState = CircuitState.CLOSED;
            log.info("Circuit breaker CLOSED after successful recovery");
        }

        double currentRate = rateLimiter.getRate();

        // Gradually increase rate after sustained success
        if (successes % 50 == 0 && currentRate < MAX_RATE) {
            double newRate = Math.min(
                MAX_RATE,
                currentRate * RECOVERY_MULTIPLIER
            );
            rateLimiter = RateLimiter.create(newRate);

            log.info(
                "Rate limit increased: {} -> {} req/s (consecutive successes: {})",
                String.format("%.2f", currentRate),
                String.format("%.2f", newRate),
                successes
            );
        }
    }

    private boolean shouldAttemptRecovery() {
        if (circuitOpenedAt == null) return false;

        return LocalDateTime.now().isAfter(
            circuitOpenedAt.plusNanos(CIRCUIT_OPEN_DURATION_MS * 1_000_000)
        );
    }

    private long calculateBackoff(int attempt) {
        // Exponential backoff: 1s, 2s, 4s, 8s...
        long baseDelay = 1000L;
        long exponentialDelay = baseDelay * (long) Math.pow(2, attempt - 1);

        // Add jitter (0-25% random variation) to prevent thundering herd
        long jitter = (long) (exponentialDelay * 0.25 * Math.random());

        return Math.min(exponentialDelay + jitter, 30_000L); // Cap at 30s
    }

    private boolean isTimeoutException(Exception e) {
        return (
            e instanceof ResourceAccessException &&
            (e.getCause() instanceof java.net.SocketTimeoutException ||
                e.getMessage().contains("timeout") ||
                e.getMessage().contains("timed out"))
        );
    }

    private MinyustFamilyResponseDto callExternalServiceWithRetry(
        MinyustFamilyRequestDto requestDto,
        int attemptNumber
    ) {
        try {
            log.info(
                "Calling external service (attempt {}): {} with body: {}",
                attemptNumber + 1,
                URL,
                requestDto
            );

            return restClient
                .post()
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve()
                .onStatus(
                    status -> status.value() == 429, // Rate limit hit
                    (request, response) -> {
                        log.warn("Rate limit (429) received from server");
                        handleTimeout(); // Treat 429 as timeout
                        throw new ResourceAccessException(
                            "Rate limit exceeded"
                        );
                    }
                )
                .onStatus(
                    HttpStatusCode::is4xxClientError,
                    (request, response) -> {
                        log.error("4xx error: {}", response.getStatusCode());
                        throw new IllegalArgumentException(
                            "Client error: " + response.getStatusCode()
                        );
                    }
                )
                .onStatus(
                    HttpStatusCode::is5xxServerError,
                    (request, response) -> {
                        log.error("5xx error: {}", response.getStatusCode());
                        throw new IllegalStateException(
                            "Server error: " + response.getStatusCode()
                        );
                    }
                )
                .body(MinyustFamilyResponseDto.class);
        } catch (ResourceAccessException e) {
            log.error("I/O error connecting to {}: {}", URL, e.getMessage());
            throw e; // Re-throw to be handled by retry logic
        }
    }

    private RestClient createRestClient() {
        SimpleClientHttpRequestFactory factory =
            new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000); // Increased from 5s
        factory.setReadTimeout(45_000); // Increased from 30s

        return RestClient.builder().requestFactory(factory).build();
    }
}
