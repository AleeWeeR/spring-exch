package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.dto.mip.PersonAbroadCheckStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadRestoreStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.repository.mip.PersonAbroadRepository;
import uz.fido.pfexchange.service.PersonAbroadService;

import java.util.Map;

/**
 * Pensiya oluvchilar holati uchun servis implementatsiyasi
 * Service implementation for pension recipient abroad status
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonAbroadServiceImpl implements PersonAbroadService {

    private final PersonAbroadRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * ENDPOINT 1: Just check status (no restoration)
     *
     * Response format:
     *   result: 1=success, 0=error
     *   status: 1=faol, 2=nofaol(chet elda), 3=nofaol(boshqa)
     */
    @Override
    public PersonAbroadCheckStatusResponseDto checkStatus(PersonAbroadStatusRequestDto requestDto) {
        String pinfl = requestDto.getData().getPinfl();
        Long wsId = requestDto.getData().getWsId();
        String inputData = convertRequestToJson(requestDto);

        log.info("Checking person status (no restore) for PINFL: {}, WS_ID: {}", pinfl, wsId);

        try {
            // Step 1: Check if person is active
            Integer activeStatus = repository.isPersonActive(pinfl);

            // Case: Person not found
            if (activeStatus == -1) {
                PersonAbroadCheckStatusResponseDto response = buildCheckResponse(0, "Pensiya oluvchilar ro'yhatida mavjud emas", wsId, null);
                logCheckRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Case: Person found and active
            if (activeStatus == 1) {
                PersonAbroadCheckStatusResponseDto response = buildCheckResponse(1, "", wsId, 1);
                logCheckRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Person is inactive - check WHY (close_desc)
            Map<String, Object> closeStatus = repository.getPersonCloseStatus(pinfl);
            String closeDesc = (String) closeStatus.get("o_Close_Desc");

            // Case: Inactive because abroad (close_desc=11)
            if ("11".equals(closeDesc)) {
                PersonAbroadCheckStatusResponseDto response = buildCheckResponse(1, "", wsId, 2);
                logCheckRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Case: Inactive for other reasons
            PersonAbroadCheckStatusResponseDto response = buildCheckResponse(1, "", wsId, 3);
            logCheckRequest(wsId, pinfl, inputData, response);
            return response;

        } catch (Exception e) {
            log.error("Error checking status for PINFL: {}", pinfl, e);
            PersonAbroadCheckStatusResponseDto response = buildCheckResponse(0, "Ma'lumotni qayta ishlashda xatolik", wsId, null);
            logCheckRequest(wsId, pinfl, inputData, response);
            throw new RuntimeException("Failed to check person status", e);
        }
    }

    /**
     * ENDPOINT 2: Check arrival and restore if needed
     *
     * Response result codes:
     *   0 = Pensiya oluvchilar ro'yhatida mavjud emas
     *   1 = Pensiya oluvchilar ro'yhatida mavjud
     *   2 = Oluvchi statusi faol xolatga keltirildi
     *   3 = O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi
     */
    @Override
    public PersonAbroadRestoreStatusResponseDto restoreStatus(PersonAbroadStatusRequestDto requestDto) {
        String pinfl = requestDto.getData().getPinfl();
        Long wsId = requestDto.getData().getWsId();
        String inputData = convertRequestToJson(requestDto);

        log.info("Checking restore status for PINFL: {}, WS_ID: {}", pinfl, wsId);

        try {
            // Step 1: Check if person exists
            Integer activeStatus = repository.isPersonActive(pinfl);

            // Case 0: Person not found
            if (activeStatus == -1) {
                PersonAbroadRestoreStatusResponseDto response = buildRestoreResponse(0, "Pensiya oluvchilar ro'yhatida mavjud emas", wsId);
                logRestoreRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Case 1: Person already active
            if (activeStatus == 1) {
                PersonAbroadRestoreStatusResponseDto response = buildRestoreResponse(1, "Pensiya oluvchilar ro'yhatida mavjud", wsId);
                logRestoreRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Person is inactive - check if they can be restored
            PersonAbroadRestoreStatusResponseDto response = checkCitizenArrivalAndRestore(pinfl, wsId, inputData);
            return response;

        } catch (Exception e) {
            log.error("Error restoring status for PINFL: {}", pinfl, e);
            PersonAbroadRestoreStatusResponseDto response = buildRestoreResponse(0, "Ma'lumotni qayta ishlashda xatolik", wsId);
            logRestoreRequest(wsId, pinfl, inputData, response);
            throw new RuntimeException("Failed to restore person status", e);
        }
    }

    /**
     * Check if citizen has arrived and restore if yes
     */
    private PersonAbroadRestoreStatusResponseDto checkCitizenArrivalAndRestore(String pinfl, Long wsId, String inputData) {
        // Get person ID and birth date
        Long personId = repository.getPersonIdByPinfl(pinfl);
        if (personId == null) {
            PersonAbroadRestoreStatusResponseDto response = buildRestoreResponse(
                0,
                "Pensiya oluvchilar ro'yhatida mavjud emas",
                wsId
            );
            logRestoreRequest(wsId, pinfl, inputData, response);
            return response;
        }

        java.sql.Date birthDate = repository.getPersonBirthDate(personId);

        // Check if citizen has arrived
        Map<String, Object> arrivalResult = repository.checkCitizenArrival(personId, pinfl, birthDate);
        Integer arrived = (Integer) arrivalResult.get("RETURN");

        if (arrived == 1) {
            // Citizen has arrived - restore them
            Map<String, Object> restoreResult = repository.restoreArrivedPerson(personId);
            Integer restored = (Integer) restoreResult.get("RETURN");

            if (restored == 1) {
                // Successfully restored
                PersonAbroadRestoreStatusResponseDto response = buildRestoreResponse(
                    2,
                    "Oluvchi statusi faol xolatga keltirildi",
                    wsId
                );
                logRestoreRequest(wsId, pinfl, inputData, response);
                log.info("Person {} successfully restored", pinfl);
                return response;
            }
        }

        // Citizen has NOT arrived
        PersonAbroadRestoreStatusResponseDto response = buildRestoreResponse(
            3,
            "O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi",
            wsId
        );
        logRestoreRequest(wsId, pinfl, inputData, response);
        return response;
    }

    /**
     * Build check status response DTO
     */
    private PersonAbroadCheckStatusResponseDto buildCheckResponse(Integer result, String msg, Long wsId, Integer status) {
        return PersonAbroadCheckStatusResponseDto.builder()
            .result(result)
            .msg(msg)
            .wsId(wsId)
            .status(status)
            .build();
    }

    /**
     * Build restore status response DTO
     */
    private PersonAbroadRestoreStatusResponseDto buildRestoreResponse(Integer result, String msg, Long wsId) {
        return PersonAbroadRestoreStatusResponseDto.builder()
            .result(result)
            .msg(msg)
            .wsId(wsId)
            .build();
    }

    /**
     * Log the check status request to database
     */
    private void logCheckRequest(Long wsId, String pinfl, String inputData, PersonAbroadCheckStatusResponseDto response) {
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
            log.warn("Failed to log check request for PINFL: {}", pinfl, e);
        }
    }

    /**
     * Log the restore status request to database
     */
    private void logRestoreRequest(Long wsId, String pinfl, String inputData, PersonAbroadRestoreStatusResponseDto response) {
        try {
            repository.logStatusRequest(
                wsId,
                pinfl,
                inputData,
                response.getResult(),
                response.getMsg(),
                null
            );
        } catch (Exception e) {
            // Don't fail the main operation if logging fails
            log.warn("Failed to log restore request for PINFL: {}", pinfl, e);
        }
    }

    /**
     * Convert request to JSON string for logging
     */
    private String convertRequestToJson(PersonAbroadStatusRequestDto request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert request to JSON", e);
            return null;
        }
    }
}
