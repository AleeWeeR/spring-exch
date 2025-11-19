package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusResponseDto;
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
     * Returns:
     * 0 - Not found
     * 1 - Active
     * 2 - Inactive (close_desc=11, abroad >3 months)
     * 3 - Inactive (other close reasons)
     */
    @Override
    public PersonAbroadStatusResponseDto checkStatus(PersonAbroadStatusRequestDto requestDto) {
        String pinfl = requestDto.getData().getPinfl();
        Long wsId = requestDto.getData().getWsId();
        String inputData = convertRequestToJson(requestDto);

        log.info("Checking person status (no restore) for PINFL: {}, WS_ID: {}", pinfl, wsId);

        PersonAbroadStatusResponseDto response;

        try {
            // Step 1: Check if person is active
            Integer activeStatus = repository.isPersonActive(pinfl);

            // Case 0: Person not found
            if (activeStatus == -1) {
                response = buildResponse(0, "Pensiya oluvchilar ro'yhatida mavjud emas", wsId, null);
                logRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Case 1: Person found and active
            if (activeStatus == 1) {
                response = buildResponse(1, "", wsId, 1);
                logRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Person is inactive - check WHY (close_desc)
            Map<String, Object> closeStatus = repository.getPersonCloseStatus(pinfl);
            String closeDesc = (String) closeStatus.get("o_Close_Desc");

            // Case 2: Inactive because abroad (close_desc=11)
            if ("11".equals(closeDesc)) {
                response = buildResponse(2, "", wsId, 0);
                logRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Case 3: Inactive for other reasons
            response = buildResponse(3, "", wsId, 0);
            logRequest(wsId, pinfl, inputData, response);
            return response;

        } catch (Exception e) {
            log.error("Error checking status for PINFL: {}", pinfl, e);
            response = buildResponse(0, "Ma'lumotni qayta ishlashda xatolik", wsId, null);
            logRequest(wsId, pinfl, inputData, response);
            throw new RuntimeException("Failed to check person status", e);
        }
    }

    /**
     * ENDPOINT 2: Check arrival and restore if needed (only for close_desc=11)
     *
     * Returns:
     * 0 - Not found
     * 1 - Found and already active
     * 2 - Person restored (was abroad, has returned)
     * 3 - Person abroad, not yet returned
     */
    @Override
    public PersonAbroadStatusResponseDto restoreStatus(PersonAbroadStatusRequestDto requestDto) {
        String pinfl = requestDto.getData().getPinfl();
        Long wsId = requestDto.getData().getWsId();
        String inputData = convertRequestToJson(requestDto);

        log.info("Checking restore status for PINFL: {}, WS_ID: {}", pinfl, wsId);

        PersonAbroadStatusResponseDto response;

        try {
            // Step 1: Check if person exists
            Integer activeStatus = repository.isPersonActive(pinfl);

            // Case 0: Person not found
            if (activeStatus == -1) {
                response = buildResponse(0, "Pensiya oluvchilar ro'yhatida mavjud emas", wsId, null);
                logRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Case 1: Person already active
            if (activeStatus == 1) {
                response = buildResponse(1, "Pensiya oluvchilar ro'yhatida mavjud", wsId, 1);
                logRequest(wsId, pinfl, inputData, response);
                return response;
            }

            // Person is inactive - check if they can be restored
            response = checkCitizenArrivalAndRestore(pinfl, wsId, inputData);
            return response;

        } catch (Exception e) {
            log.error("Error restoring status for PINFL: {}", pinfl, e);
            response = buildResponse(0, "Ma'lumotni qayta ishlashda xatolik", wsId, null);
            logRequest(wsId, pinfl, inputData, response);
            throw new RuntimeException("Failed to restore person status", e);
        }
    }

    /**
     * Check if citizen has arrived and restore if yes
     */
    private PersonAbroadStatusResponseDto checkCitizenArrivalAndRestore(String pinfl, Long wsId, String inputData) {
        // Get person ID and birth date
        Long personId = repository.getPersonIdByPinfl(pinfl);
        if (personId == null) {
            PersonAbroadStatusResponseDto response = buildResponse(
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

        if (arrived == 1) {
            // Citizen has arrived - restore them
            Map<String, Object> restoreResult = repository.restoreArrivedPerson(personId);
            Integer restored = (Integer) restoreResult.get("RETURN");

            if (restored == 1) {
                // Successfully restored
                PersonAbroadStatusResponseDto response = buildResponse(
                    2,
                    "Oluvchi statusi faol xolatga keltirildi",
                    wsId,
                    null
                );
                logRequest(wsId, pinfl, inputData, response);
                log.info("Person {} successfully restored", pinfl);
                return response;
            }
        }

        // Citizen has NOT arrived
        PersonAbroadStatusResponseDto response = buildResponse(
            3,
            "O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi",
            wsId,
            0
        );
        logRequest(wsId, pinfl, inputData, response);
        return response;
    }

    /**
     * Build response DTO
     */
    private PersonAbroadStatusResponseDto buildResponse(Integer result, String msg, Long wsId, Integer status) {
        return PersonAbroadStatusResponseDto.builder()
            .result(result)
            .msg(msg)
            .wsId(wsId)
            .status(status)
            .build();
    }

    /**
     * Log the request to database
     */
    private void logRequest(Long wsId, String pinfl, String inputData, PersonAbroadStatusResponseDto response) {
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
    private String convertRequestToJson(PersonAbroadStatusRequestDto request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert request to JSON", e);
            return null;
        }
    }
}
