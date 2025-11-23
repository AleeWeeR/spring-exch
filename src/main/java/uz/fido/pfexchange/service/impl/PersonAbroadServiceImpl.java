package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Clob;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.dto.mip.PersonAbroadCheckStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadRestoreStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.repository.mip.PersonAbroadRepository;
import uz.fido.pfexchange.service.PersonAbroadService;

/**
 * Pensiya oluvchilar holati uchun servis implementatsiyasi
 * Service implementation for pension recipient abroad status
 *
 * This service calls PF_EXCHANGES_ABROAD package functions which handle
 * all business logic and database logging internally.
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
     * Calls PF_EXCHANGES_ABROAD.Check_Person_Status
     * Returns JSON: {"result": 1, "msg": "", "ws_id": 77, "status": 1}
     */
    @Override
    public PersonAbroadCheckStatusResponseDto checkStatus(PersonAbroadStatusRequestDto requestDto) {
        Long wsId = requestDto.getData().getWsId();
        String pinfl = requestDto.getData().getPinfl();

        log.info("Checking person status (no restore) for PINFL: {}, WS_ID: {}", pinfl, wsId);

        try {
            // Convert request to XML format (Oracle expects XML)
            String xmlData = convertToXml(requestDto);

            // Call Oracle function
            Map<String, Object> result = repository.checkPersonStatus(xmlData);

            // Parse JSON response from CLOB
            Clob responseClob = (Clob) result.get("O_Data");
            String jsonResponse = repository.clobToString(responseClob);

            log.debug("Oracle response JSON: {}", jsonResponse);

            // Parse JSON to DTO
            PersonAbroadCheckStatusResponseDto response = objectMapper.readValue(
                jsonResponse,
                PersonAbroadCheckStatusResponseDto.class
            );

            log.info("Check status completed - result: {}, status: {}", response.getResult(), response.getStatus());

            return response;

        } catch (Exception e) {
            log.error("Error checking status for PINFL: {}", pinfl, e);
            // Return error response
            return PersonAbroadCheckStatusResponseDto.builder()
                .result(0)
                .msg("Ma'lumotni qayta ishlashda xatolik: " + e.getMessage())
                .wsId(wsId)
                .status(null)
                .build();
        }
    }

    /**
     * ENDPOINT 2: Check arrival and restore if needed
     *
     * Calls PF_EXCHANGES_ABROAD.Restore_Person_Status
     * Returns JSON: {"result": 2, "msg": "O'zgartirildi", "ws_id": 77}
     */
    @Override
    public PersonAbroadRestoreStatusResponseDto restoreStatus(PersonAbroadStatusRequestDto requestDto) {
        Long wsId = requestDto.getData().getWsId();
        String pinfl = requestDto.getData().getPinfl();

        log.info("Checking restore status for PINFL: {}, WS_ID: {}", pinfl, wsId);

        try {
            // Convert request to XML format (Oracle expects XML)
            String xmlData = convertToXml(requestDto);

            // Call Oracle function
            Map<String, Object> result = repository.restorePersonStatus(xmlData);

            // Parse JSON response from CLOB
            Clob responseClob = (Clob) result.get("O_Data");
            String jsonResponse = repository.clobToString(responseClob);

            log.debug("Oracle response JSON: {}", jsonResponse);

            // Parse JSON to DTO
            PersonAbroadRestoreStatusResponseDto response = objectMapper.readValue(
                jsonResponse,
                PersonAbroadRestoreStatusResponseDto.class
            );

            log.info("Restore status completed - result: {}, message: {}",
                response.getResult(),
                response.getMsg()
            );

            return response;

        } catch (Exception e) {
            log.error("Error restoring status for PINFL: {}", pinfl, e);
            // Return error response
            return PersonAbroadRestoreStatusResponseDto.builder()
                .result(0)
                .msg("Ma'lumotni qayta ishlashda xatolik: " + e.getMessage())
                .wsId(wsId)
                .build();
        }
    }

    /**
     * Convert request DTO to XML format expected by Oracle
     * Format: <Data><ws_id>77</ws_id><pinfl>41006673910061</pinfl></Data>
     */
    private String convertToXml(PersonAbroadStatusRequestDto requestDto) {
        return String.format(
            "<Data><ws_id>%d</ws_id><pinfl>%s</pinfl></Data>",
            requestDto.getData().getWsId(),
            requestDto.getData().getPinfl()
        );
    }
}
