package uz.fido.pfexchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.config.properties.MinyustBatchProperties;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.minyust.MinyustFamilyBatchResponseDto;
import uz.fido.pfexchange.dto.minyust.ProcessingResult;
import uz.fido.pfexchange.dto.minyust.ProcessingState;
import uz.fido.pfexchange.enums.MinyustState;
import uz.fido.pfexchange.service.minyust.MinyustFamilyBatchRequestProcessor;
import uz.fido.pfexchange.service.minyust.MinyustFamilyContinuousProcessingService;
import uz.fido.pfexchange.utils.MinyustFamilyBatchStatus;
import uz.fido.pfexchange.utils.ResponseBuilder;
import static uz.fido.pfexchange.config.Authority.Codes.*;

import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pf/minyust/family/batch")
@Tag(
    name = "Minyust Oilaviy Ma'lumotlar",
    description = "Minyust oilaviy ma'lumotlarni batch rejimida qayta ishlash API'lari"
)
public class MinyustFamilyController {

    private final MinyustBatchProperties properties;
    private final MinyustFamilyBatchRequestProcessor processor;
    private final MinyustFamilyContinuousProcessingService continuousProcessingService;

    @PostMapping("/process-one-batch")
    @PreAuthorize("hasAuthority('" + INTERNAL_MINYUST_FAMILY_PROCESS_ONE_BATCH + "')")
    @Operation(
        summary = "Bitta batch ni qayta ishlash",
        description = "Navbatdagi bitta batch'ni qayta ishlaydi va natijani qaytaradi."
    )
    public ResponseEntity<ResponseWrapperDto<MinyustFamilyBatchResponseDto>> processOneBatch() {
        ProcessingResult result = processor.processOneBatch();

        MinyustFamilyBatchResponseDto response = buildResponse(
            result.isSuccess() ? MinyustFamilyBatchStatus.COMPLETED : MinyustFamilyBatchStatus.FAILED,
            (result.isSuccess() ? "Batch completed: " : "Batch failed: ") + result.getMessage()
        );
        response.setStatusSummary(processor.getStatusSummary());

        return ResponseBuilder.ok(response);
    }

    @PostMapping("/start-continuous")
    @PreAuthorize("hasAuthority('" + INTERNAL_MINYUST_FAMILY_START_PROCESSING + "')")
    @Operation(
        summary = "Uzluksiz qayta ishlashni boshlash",
        description = "Barcha kutilayotgan so'rovlarni uzluksiz qayta ishlashni boshlaydi."
    )
    public ResponseEntity<ResponseWrapperDto<MinyustFamilyBatchResponseDto>> startContinuousProcessing() {
        boolean started = continuousProcessingService.start();

        MinyustFamilyBatchResponseDto response = buildResponse(
            started ? MinyustFamilyBatchStatus.STARTED : MinyustFamilyBatchStatus.ALREADY_RUNNING,
            started 
                ? "Continuous processing started. Will process all batches until complete."
                : "Continuous processing is already in progress"
        );

        return ResponseBuilder.ok(response);
    }

    @PostMapping("/stop")
    @PreAuthorize("hasAuthority('" + INTERNAL_MINYUST_FAMILY_STOP_PROCESSING + "')")
    @Operation(
        summary = "Qayta ishlashni to'xtatish",
        description = "Joriy uzluksiz qayta ishlash jarayonini to'xtatadi."
    )
    public ResponseEntity<ResponseWrapperDto<MinyustFamilyBatchResponseDto>> stopProcessing() {
        boolean stopped = continuousProcessingService.stop();

        MinyustFamilyBatchResponseDto response = buildResponse(
            stopped ? MinyustFamilyBatchStatus.STOPPED : MinyustFamilyBatchStatus.IDLE,
            stopped ? "Continuous processing interrupted" : "No active continuous processing to stop"
        );

        return ResponseBuilder.ok(response);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('" + INTERNAL_MINYUST_FAMILY_GET_STATUS + "')")
    @Operation(
        summary = "Jarayon holatini olish",
        description = "Joriy qayta ishlash jarayonining holatini qaytaradi."
    )
    public ResponseEntity<ResponseWrapperDto<MinyustFamilyBatchResponseDto>> getStatus() {
        ProcessingState state = continuousProcessingService.getState();

        MinyustFamilyBatchStatus status = mapStateToStatus(state.state());
        String message = buildStatusMessage(state);

        MinyustFamilyBatchResponseDto response = buildResponse(status, message);
        response.setStatusSummary(processor.getStatusSummary());

        return ResponseBuilder.ok(response);
    }

    @GetMapping("/progress")
    @PreAuthorize("hasAuthority('" + INTERNAL_MINYUST_FAMILY_GET_PROGRESS + "')")
    @Operation(
        summary = "Jarayon progressini olish",
        description = "Batch qayta ishlash jarayonining batafsil progressini qaytaradi."
    )
    public ResponseEntity<Map<String, Object>> getProgress() {
        Map<String, Long> statusCounts = processor.getStatusSummary();

        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        long completed = statusCounts.getOrDefault("COMPLETED", 0L);
        long different = statusCounts.getOrDefault("DIFFERENT", 0L);
        long failed = statusCounts.getOrDefault("FAILED", 0L);
        long notFound = statusCounts.getOrDefault("NOT_FOUND", 0L);
        long processed = completed + different + failed + notFound;

        return ResponseEntity.ok(Map.of(
            "total", total,
            "processed", processed,
            "percentComplete", String.format("%.2f", total > 0 ? (processed * 100.0 / total) : 0),
            "ready", statusCounts.getOrDefault("READY", 0L),
            "processing", statusCounts.getOrDefault("PROCESSING", 0L),
            "completed", completed,
            "different", different,
            "failed", failed,
            "notFound", notFound,
            "scheduledMode", properties.isScheduledEnabled()
        ));
    }

    @PostMapping("/recover-stuck")
    @PreAuthorize("hasAuthority('" + INTERNAL_MINYUST_FAMILY_RECOVER_STUCK + "')")
    @Operation(
        summary = "Tiqilib qolgan yozuvlarni tiklash",
        description = "PROCESSING holatida qolib ketgan yozuvlarni READY holatiga qaytaradi."
    )
    public ResponseEntity<Map<String, Object>> recoverStuckRecords() {
        int recovered = processor.recoverStuckRecords();

        return ResponseEntity.ok(Map.of(
            "recovered", recovered,
            "message", recovered > 0
                ? "Recovered " + recovered + " stuck records"
                : "No stuck records found"
        ));
    }

    @GetMapping("/config")
    @PreAuthorize("hasAuthority('" + INTERNAL_MINYUST_FAMILY_CONFIG + "')")
    @Operation(
        summary = "Konfiguratsiya sozlamalarini olish",
        description = "Joriy batch qayta ishlash konfiguratsiyasini qaytaradi."
    )
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of(
            "scheduledEnabled", properties.isScheduledEnabled(),
            "batchSize", properties.getBatchSize(),
            "threadPoolSize", properties.getThreadPoolSize(),
            "rateLimitPerSecond", properties.getRateLimitPerSecond(),
            "maxRetries", properties.getMaxRetries(),
            "batchTimeoutMinutes", properties.getBatchTimeoutMinutes(),
            "stuckRecordThresholdMinutes", properties.getStuckRecordThresholdMinutes()
        ));
    }

    private MinyustFamilyBatchResponseDto buildResponse(MinyustFamilyBatchStatus status, String message) {
        MinyustFamilyBatchResponseDto response = new MinyustFamilyBatchResponseDto();
        response.setStatus(status);
        response.setMessage(message);
        response.setPendingCount(processor.getPendingCount());
        return response;
    }

    private MinyustFamilyBatchStatus mapStateToStatus(MinyustState state) {
        return switch (state) {
            case RUNNING -> MinyustFamilyBatchStatus.PROCESSING;
            case STOPPED_UNEXPECTEDLY, IDLE -> MinyustFamilyBatchStatus.IDLE;
        };
    }

    private String buildStatusMessage(ProcessingState state) {
        return switch (state.state()) {
            case RUNNING -> "Continuous processing is running on thread: " + state.threadName();
            case STOPPED_UNEXPECTEDLY -> "Processing stopped unexpectedly";
            case IDLE -> "No processing running";
        };
    }
}