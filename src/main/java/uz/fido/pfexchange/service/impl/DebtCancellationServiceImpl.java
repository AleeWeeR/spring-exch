package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.mib.*;
import uz.fido.pfexchange.entity.PfExchangeMibDelDebt;
import uz.fido.pfexchange.entity.PfMibCancelRel;
import uz.fido.pfexchange.exceptioning.RestException;
import uz.fido.pfexchange.repository.PfExchangeMibDelDebtRepository;
import uz.fido.pfexchange.repository.PfMibCancelRelRepository;
import uz.fido.pfexchange.service.DebtCancellationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for debt cancellation with MIB pension system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebtCancellationServiceImpl implements DebtCancellationService {

    private final PfMibCancelRelRepository cancelRelRepository;
    private final PfExchangeMibDelDebtRepository delDebtRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Value("${mib.pension.cancel-inventory.url:https://pension.mib.uz/cancel-inventory}")
    private String mibCancelUrl;

    @Value("${mib.pension.cancel-inventory.auth:Basic cGVuc2lvbjpxcFtYJDM5JG5bdS5yZS40}")
    private String mibAuthHeader;

    @Override
    @Transactional
    public DebtCancellationResponseDto cancelDebt(DebtCancellationRequestDto requestDto) {
        Long externalId = requestDto.getExternalId();
        log.info("Canceling debt for external_id: {}", externalId);

        // Find the cancellation request record
        Optional<PfMibCancelRel> cancelRelOpt = cancelRelRepository.findByExternalId(externalId);
        if (cancelRelOpt.isEmpty()) {
            log.warn("No cancellation record found for external_id: {}", externalId);
            return DebtCancellationResponseDto.builder()
                .result(0)
                .msg("Qarzdorlik bekor qilish so'rovi topilmadi")
                .externalId(externalId)
                .isSent("N")
                .isCancelled("N")
                .build();
        }

        PfMibCancelRel cancelRel = cancelRelOpt.get();

        // Check if already sent
        if ("Y".equals(cancelRel.getIsSent())) {
            log.info("Debt cancellation already sent for external_id: {}", externalId);
            return DebtCancellationResponseDto.builder()
                .result(1)
                .msg(cancelRel.getCommentText())
                .externalId(externalId)
                .isSent(cancelRel.getIsSent())
                .isCancelled(cancelRel.getIsCancelled())
                .build();
        }

        try {
            // Build payload for MIB API
            MibCancelDebtPayloadDto payload = buildMibPayload(cancelRel);
            log.debug("MIB payload: {}", payload);

            // Call MIB API
            MibCancelDebtResponseDto mibResponse = callMibCancelApi(payload);
            log.info("MIB response - code: {}, message: {}",
                mibResponse.getResultCode(), mibResponse.getResultMessage());

            // Update database with result
            return processResponse(cancelRel, mibResponse, payload);

        } catch (Exception e) {
            log.error("Error canceling debt for external_id: {}", externalId, e);

            // Update record with error
            cancelRel.setCommentText("Xatolik: " + e.getMessage());
            cancelRel.setLastUpdateDate(LocalDateTime.now());
            cancelRelRepository.save(cancelRel);

            return DebtCancellationResponseDto.builder()
                .result(0)
                .msg("Qarzdorlikni bekor qilishda xatolik: " + e.getMessage())
                .externalId(externalId)
                .isSent("N")
                .isCancelled("N")
                .build();
        }
    }

    @Override
    @Transactional
    public void autoCheckAndCancelDebts(String pinpp, String closeReason) {
        log.info("Auto-checking debts for PINPP: {}, close reason: {}", pinpp, closeReason);

        // Find all active debts for this person
        List<PfExchangeMibDelDebt> activeDebts = delDebtRepository.findByPinpp(pinpp);

        if (activeDebts.isEmpty()) {
            log.info("No active debts found for PINPP: {}", pinpp);
            return;
        }

        // Find unsent cancellation records for this person
        List<PfMibCancelRel> unsentCancellations = cancelRelRepository.findUnsentByPinpp(pinpp);

        for (PfMibCancelRel cancelRel : unsentCancellations) {
            try {
                DebtCancellationRequestDto request = new DebtCancellationRequestDto(cancelRel.getExternalId());
                cancelDebt(request);
            } catch (Exception e) {
                log.error("Error auto-canceling debt for external_id: {}", cancelRel.getExternalId(), e);
            }
        }
    }

    /**
     * Build MIB API payload from database record
     */
    private MibCancelDebtPayloadDto buildMibPayload(PfMibCancelRel cancelRel) {
        // Query person information (simplified - in real scenario, join with pf_persons table)
        // For now, we'll use what's available in the cancelRel record

        Integer reasonId = determineReasonId(cancelRel);
        String reasonName = determineReasonName(cancelRel, reasonId);

        return MibCancelDebtPayloadDto.builder()
            .inventoryId(cancelRel.getExternalId())
            .fioPerformer("System") // This should come from person record
            .phonePerformer("") // This should come from person record
            .reasonId(reasonId)
            .reasonName(reasonName)
            .build();
    }

    /**
     * Determine reason ID from close description
     */
    private Integer determineReasonId(PfMibCancelRel cancelRel) {
        // Based on the PL/SQL logic:
        // when p.close_desc = 02 then 1
        // when p.close_desc = 03 then 2
        // when p.close_desc = 08 then 4
        // when a.close_reason = 01 then 1
        // when a.close_reason = 03 then 3
        // when a.close_reason = 05 then 5

        // Default to 1 if not specified
        return 1;
    }

    /**
     * Determine reason name from close description
     */
    private String determineReasonName(PfMibCancelRel cancelRel, Integer reasonId) {
        // This should come from pf_s_close_reasons or pf_s_app_close_reasons table
        // For now, return a default
        return "Bekor qilish sababi: " + reasonId;
    }

    /**
     * Call MIB pension cancel-inventory API
     */
    private MibCancelDebtResponseDto callMibCancelApi(MibCancelDebtPayloadDto payload) {
        try {
            log.info("Calling MIB cancel API: {} with payload: {}", mibCancelUrl, payload);

            return restClient
                .post()
                .uri(mibCancelUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", mibAuthHeader)
                .body(payload)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                    int statusCode = response.getStatusCode().value();
                    log.error("4xx error from MIB service: {}", statusCode);

                    String errorMessage = switch (statusCode) {
                        case 400 -> "MIB servisiga noto'g'ri so'rov";
                        case 401 -> "MIB servisiga ruxsatsiz kirish";
                        case 403 -> "MIB servisiga kirish taqiqlangan";
                        case 404 -> "MIB servis endpoint topilmadi";
                        default -> "MIB servis xatoligi: " + statusCode;
                    };

                    throw RestException.restThrow(
                        ResponseWrapperDto.<Void>builder()
                            .code(Constants.ERROR)
                            .message(errorMessage)
                            .build(),
                        HttpStatus.valueOf(statusCode)
                    );
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, response) -> {
                    int statusCode = response.getStatusCode().value();
                    log.error("5xx error from MIB service: {}", statusCode);

                    throw RestException.restThrow(
                        ResponseWrapperDto.<Void>builder()
                            .code(Constants.ERROR)
                            .message("MIB servisi vaqtincha ishlamayapti")
                            .build(),
                        HttpStatus.BAD_GATEWAY
                    );
                })
                .body(MibCancelDebtResponseDto.class);
        } catch (ResourceAccessException e) {
            log.error("Connection error to MIB service: {}", mibCancelUrl, e);
            log.error("Root cause: ", e.getCause());

            Throwable rootCause = e.getRootCause();
            throw RestException.restThrow(
                ResponseWrapperDto.<Void>builder()
                    .code(503)
                    .message(
                        "MIB servisiga ulanishda xatolik: " +
                            (rootCause != null ? rootCause.getMessage() : "Noma'lum xatolik")
                    )
                    .build(),
                HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    /**
     * Process MIB response and update database
     */
    @Transactional
    private DebtCancellationResponseDto processResponse(
        PfMibCancelRel cancelRel,
        MibCancelDebtResponseDto mibResponse,
        MibCancelDebtPayloadDto payload
    ) {
        Integer resultCode = mibResponse.getResultCode();
        String resultMessage = mibResponse.getResultMessage();

        // Update cancelRel record
        cancelRel.setIsSent("Y");
        cancelRel.setDataOut(convertPayloadToJson(payload));
        cancelRel.setCommentText(String.format("result_code: %d; result msg: %s",
            resultCode, resultMessage));
        cancelRel.setLastUpdateDate(LocalDateTime.now());

        // Check if cancellation was successful (code 0 or 7 based on PL/SQL)
        if (resultCode == 0 || resultCode == 7) {
            cancelRel.setIsCancelled("Y");

            // Update debt records
            if (resultCode == 0) {
                List<PfExchangeMibDelDebt> debts = delDebtRepository.findByExternalId(cancelRel.getExternalId());
                for (PfExchangeMibDelDebt debt : debts) {
                    debt.setCanceledByPfId(cancelRel.getMibCancelRelId());
                    debt.setLastUpdateDate(LocalDateTime.now());
                }
                delDebtRepository.saveAll(debts);
            } else if (resultCode == 7) {
                // Already cancelled - check if debt records exist
                Long debtCount = delDebtRepository.countByExternalId(cancelRel.getExternalId());
                if (debtCount == 0) {
                    cancelRel.setCommentText(cancelRel.getCommentText() +
                        "; External id: " + cancelRel.getExternalId() + " allaqachon bekor qilingan");
                }
            }
        } else {
            cancelRel.setIsCancelled("N");
        }

        cancelRelRepository.save(cancelRel);

        return DebtCancellationResponseDto.builder()
            .result(resultCode == 0 || resultCode == 7 ? 1 : 0)
            .msg(resultMessage)
            .externalId(cancelRel.getExternalId())
            .isSent(cancelRel.getIsSent())
            .isCancelled(cancelRel.getIsCancelled())
            .build();
    }

    /**
     * Convert payload to JSON string for storage
     */
    private String convertPayloadToJson(MibCancelDebtPayloadDto payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Error converting payload to JSON", e);
            return payload.toString();
        }
    }
}
