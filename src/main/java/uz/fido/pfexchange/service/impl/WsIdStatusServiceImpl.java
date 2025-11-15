package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.dto.mip.WsIdStatusRequestDto;
import uz.fido.pfexchange.dto.mip.WsIdStatusResponseDto;
import uz.fido.pfexchange.repository.mip.WsIdStatusRepository;
import uz.fido.pfexchange.service.WsIdStatusService;

import java.util.Map;

/**
 * Pensiya oluvchilar holati uchun servis implementatsiyasi
 * Service implementation for pension recipient status
 *
 * This service orchestrates multiple repository calls to determine person status
 *
 * Natija kodlari:
 * 0 - Pensiya oluvchilar ro'yhatida mavjud emas
 * 1 - Pensiya oluvchilar ro'yhatida mavjud
 * 2 - Oluvchi statusi faol xolatga keltirildi
 * 3 - O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WsIdStatusServiceImpl implements WsIdStatusService {

    private final WsIdStatusRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public WsIdStatusResponseDto checkStatus(WsIdStatusRequestDto requestDto) {
        String pinfl = requestDto.getData().getPinfl();
        Long wsId = requestDto.getData().getWsId();
        String inputData = convertRequestToJson(requestDto);

        log.info("Checking pension recipient status for PINFL: {}, WS_ID: {}", pinfl, wsId);

        WsIdStatusResponseDto response;

        try {
            // Step 1: Check if person is active
            Integer activeStatus = repository.isPersonActive(pinfl);

            // Case 1: Person not found
            if (activeStatus == -1) {
                response = buildResponse(0, "Pensiya oluvchilar ro'yhatida mavjud emas", wsId, null);
                logRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Case 2: Person found and active
            if (activeStatus == 1) {
                response = buildResponse(1, "", wsId, 1);
                logRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Case 3: Person found but inactive - check citizenship arrival
            response = handleInactivePerson(pinfl, wsId, inputData);
            return response;

        } catch (Exception e) {
            log.error("Error checking status for PINFL: {}", pinfl, e);
            response = buildResponse(0, "Ma'lumotni qayta ishlashda xatolik", wsId, null);
            logRequest(wsId, pinfl, inputData, response);
            throw new RuntimeException("Failed to check person status", e);
        }
    }

    /**
     * Handle inactive person - check if they've arrived and restore if needed
     */
    private WsIdStatusResponseDto handleInactivePerson(String pinfl, Long wsId, String inputData) {
        // Get person close status
        Map<String, Object> closeStatus = repository.getPersonCloseStatus(pinfl);
        Integer found = (Integer) closeStatus.get("RETURN");

        if (found == 0) {
            // Person data issue
            WsIdStatusResponseDto response = buildResponse(
                0,
                "Pensiya oluvchilar ro'yhatida mavjud emas",
                wsId,
                null
            );
            logRequest(wsId, pinfl, inputData, response);
            return response;
        }

        String closeReason = (String) closeStatus.get("o_Close_Reason");
        java.sql.Date closeDate = (java.sql.Date) closeStatus.get("o_Close_Date");
        String closeDesc = (String) closeStatus.get("o_Close_Desc");

        // Check if person is closed due to being abroad (like in your existing logic)
        if (closeReason != null || closeDate != null || "11".equals(closeDesc)) {
            return checkCitizenArrivalAndRestore(pinfl, wsId, inputData);
        } else {
            // Person inactive for other reasons
            WsIdStatusResponseDto response = buildResponse(
                3,
                "O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi",
                wsId,
                0
            );
            logRequest(wsId, pinfl, inputData, response);
            return response;
        }
    }

    /**
     * Check if citizen has arrived and restore if yes
     */
    private WsIdStatusResponseDto checkCitizenArrivalAndRestore(String pinfl, Long wsId, String inputData) {
        // Get person ID and birth date
        Long personId = repository.getPersonIdByPinfl(pinfl);
        if (personId == null) {
            WsIdStatusResponseDto response = buildResponse(
                0,
                "Pensiya oluvchilar ro'yhatida mavjud emas",
                wsId,
                null
            );
            logRequest(wsId, pinfl, inputData, response);
            return response;
        }

        java.sql.Date birthDate = repository.getPersonBirthDate(personId);

        // Check if citizen has arrived
        Map<String, Object> arrivalResult = repository.checkCitizenArrival(personId, pinfl, birthDate);
        Integer arrived = (Integer) arrivalResult.get("RETURN");
        String arrivalMessage = (String) arrivalResult.get("o_Message");

        if (arrived == 1) {
            // Citizen has arrived - restore them
            Map<String, Object> restoreResult = repository.restoreArrivedPerson(personId);
            Integer restored = (Integer) restoreResult.get("RETURN");
            String restoreMessage = (String) restoreResult.get("o_Message");

            if (restored == 1) {
                // Successfully restored
                WsIdStatusResponseDto response = buildResponse(2, "O'zgartirildi", wsId, null);
                logRequest(wsId, pinfl, inputData, response);
                log.info("Person {} successfully restored", pinfl);
                return response;
            } else {
                // Restore failed but person arrived
                WsIdStatusResponseDto response = buildResponse(
                    2,
                    restoreMessage != null ? restoreMessage : "O'zgartirildi",
                    wsId,
                    null
                );
                logRequest(wsId, pinfl, inputData, response);
                return response;
            }
        } else {
            // Citizen has NOT arrived
            WsIdStatusResponseDto response = buildResponse(
                3,
                "O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi",
                wsId,
                0
            );
            logRequest(wsId, pinfl, inputData, response);
            return response;
        }
    }

    /**
     * Build response DTO
     */
    private WsIdStatusResponseDto buildResponse(Integer result, String msg, Long wsId, Integer status) {
        return WsIdStatusResponseDto.builder()
            .result(result)
            .msg(msg)
            .wsId(wsId)
            .status(status)
            .build();
    }

    /**
     * Log the request to database
     */
    private void logRequest(Long wsId, String pinfl, String inputData, WsIdStatusResponseDto response) {
        try {
            repository.logStatusRequest(
                wsId,
                pinfl,
                inputData,
                response.getResult(),
                response.getMsg(),
                response.getStatus()
            );
        } catch (Exception e) {
            // Don't fail the main operation if logging fails
            log.warn("Failed to log request for PINFL: {}", pinfl, e);
        }
    }

    /**
     * Convert request to JSON string for logging
     */
    private String convertRequestToJson(WsIdStatusRequestDto request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert request to JSON", e);
            return null;
        }
    }
}
