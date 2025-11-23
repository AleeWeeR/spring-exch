package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.fido.pfexchange.dto.mip.PersonAbroadCheckStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadRestoreStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusDataDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.repository.mip.PersonAbroadRepository;

import javax.sql.rowset.serial.SerialClob;
import java.sql.Clob;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PersonAbroadServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Person Abroad Service Tests")
class PersonAbroadServiceImplTest {

    @Mock
    private PersonAbroadRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PersonAbroadServiceImpl service;

    private PersonAbroadStatusRequestDto requestDto;
    private PersonAbroadStatusDataDto dataDto;

    @BeforeEach
    void setUp() {
        dataDto = PersonAbroadStatusDataDto.builder()
                .wsId(77L)
                .pinfl("12345678901234")
                .build();

        requestDto = PersonAbroadStatusRequestDto.builder()
                .data(dataDto)
                .build();
    }

    // ========================================================================
    // Check Status Tests
    // ========================================================================

    @Test
    @DisplayName("Check Status: Should return active person (status=1)")
    void checkStatus_shouldReturnActivePerson() throws Exception {
        // Given
        String oracleJsonResponse = "{\"result\": 1, \"msg\": \"\", \"ws_id\": 77, \"status\": 1}";
        Clob responseClob = new SerialClob(oracleJsonResponse.toCharArray());

        Map<String, Object> oracleResult = new HashMap<>();
        oracleResult.put("RETURN", 1);
        oracleResult.put("O_Data", responseClob);

        PersonAbroadCheckStatusResponseDto expectedResponse = PersonAbroadCheckStatusResponseDto.builder()
                .result(1)
                .msg("")
                .wsId(77L)
                .status(1)
                .build();

        when(repository.checkPersonStatus(anyString())).thenReturn(oracleResult);
        when(repository.clobToString(responseClob)).thenReturn(oracleJsonResponse);
        when(objectMapper.readValue(oracleJsonResponse, PersonAbroadCheckStatusResponseDto.class))
                .thenReturn(expectedResponse);

        // When
        PersonAbroadCheckStatusResponseDto response = service.checkStatus(requestDto);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getResult());
        assertEquals("", response.getMsg());
        assertEquals(77L, response.getWsId());
        assertEquals(1, response.getStatus());

        verify(repository).checkPersonStatus(anyString());
        verify(objectMapper).readValue(anyString(), eq(PersonAbroadCheckStatusResponseDto.class));
    }

    @Test
    @DisplayName("Check Status: Should return abroad person (status=2)")
    void checkStatus_shouldReturnAbroadPerson() throws Exception {
        // Given
        String oracleJsonResponse = "{\"result\": 1, \"msg\": \"\", \"ws_id\": 77, \"status\": 2}";
        Clob responseClob = new SerialClob(oracleJsonResponse.toCharArray());

        Map<String, Object> oracleResult = new HashMap<>();
        oracleResult.put("RETURN", 1);
        oracleResult.put("O_Data", responseClob);

        PersonAbroadCheckStatusResponseDto expectedResponse = PersonAbroadCheckStatusResponseDto.builder()
                .result(1)
                .msg("")
                .wsId(77L)
                .status(2)
                .build();

        when(repository.checkPersonStatus(anyString())).thenReturn(oracleResult);
        when(repository.clobToString(responseClob)).thenReturn(oracleJsonResponse);
        when(objectMapper.readValue(oracleJsonResponse, PersonAbroadCheckStatusResponseDto.class))
                .thenReturn(expectedResponse);

        // When
        PersonAbroadCheckStatusResponseDto response = service.checkStatus(requestDto);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getResult());
        assertEquals(2, response.getStatus());
    }

    @Test
    @DisplayName("Check Status: Should return inactive person (status=3)")
    void checkStatus_shouldReturnInactivePerson() throws Exception {
        // Given
        String oracleJsonResponse = "{\"result\": 1, \"msg\": \"\", \"ws_id\": 77, \"status\": 3}";
        Clob responseClob = new SerialClob(oracleJsonResponse.toCharArray());

        Map<String, Object> oracleResult = new HashMap<>();
        oracleResult.put("RETURN", 1);
        oracleResult.put("O_Data", responseClob);

        PersonAbroadCheckStatusResponseDto expectedResponse = PersonAbroadCheckStatusResponseDto.builder()
                .result(1)
                .msg("")
                .wsId(77L)
                .status(3)
                .build();

        when(repository.checkPersonStatus(anyString())).thenReturn(oracleResult);
        when(repository.clobToString(responseClob)).thenReturn(oracleJsonResponse);
        when(objectMapper.readValue(oracleJsonResponse, PersonAbroadCheckStatusResponseDto.class))
                .thenReturn(expectedResponse);

        // When
        PersonAbroadCheckStatusResponseDto response = service.checkStatus(requestDto);

        // Then
        assertEquals(3, response.getStatus());
    }

    @Test
    @DisplayName("Check Status: Should return error when person not found")
    void checkStatus_shouldReturnErrorWhenPersonNotFound() throws Exception {
        // Given
        String oracleJsonResponse = "{\"result\": 0, \"msg\": \"Pensiya oluvchilar ro'yhatida mavjud emas\", \"ws_id\": 77, \"status\": null}";
        Clob responseClob = new SerialClob(oracleJsonResponse.toCharArray());

        Map<String, Object> oracleResult = new HashMap<>();
        oracleResult.put("RETURN", 0);
        oracleResult.put("O_Data", responseClob);

        PersonAbroadCheckStatusResponseDto expectedResponse = PersonAbroadCheckStatusResponseDto.builder()
                .result(0)
                .msg("Pensiya oluvchilar ro'yhatida mavjud emas")
                .wsId(77L)
                .status(null)
                .build();

        when(repository.checkPersonStatus(anyString())).thenReturn(oracleResult);
        when(repository.clobToString(responseClob)).thenReturn(oracleJsonResponse);
        when(objectMapper.readValue(oracleJsonResponse, PersonAbroadCheckStatusResponseDto.class))
                .thenReturn(expectedResponse);

        // When
        PersonAbroadCheckStatusResponseDto response = service.checkStatus(requestDto);

        // Then
        assertEquals(0, response.getResult());
        assertNotNull(response.getMsg());
        assertNull(response.getStatus());
    }

    @Test
    @DisplayName("Check Status: Should handle Oracle exception gracefully")
    void checkStatus_shouldHandleOracleException() {
        // Given
        when(repository.checkPersonStatus(anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        PersonAbroadCheckStatusResponseDto response = service.checkStatus(requestDto);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getResult());
        assertTrue(response.getMsg().contains("Ma'lumotni qayta ishlashda xatolik"));
        assertEquals(77L, response.getWsId());
        assertNull(response.getStatus());
    }

    // ========================================================================
    // Restore Status Tests
    // ========================================================================

    @Test
    @DisplayName("Restore Status: Should restore person successfully (result=2)")
    void restoreStatus_shouldRestorePersonSuccessfully() throws Exception {
        // Given
        String oracleJsonResponse = "{\"result\": 2, \"msg\": \"Oluvchi statusi faol xolatga keltirildi\", \"ws_id\": 77}";
        Clob responseClob = new SerialClob(oracleJsonResponse.toCharArray());

        Map<String, Object> oracleResult = new HashMap<>();
        oracleResult.put("RETURN", 1);
        oracleResult.put("O_Data", responseClob);

        PersonAbroadRestoreStatusResponseDto expectedResponse = PersonAbroadRestoreStatusResponseDto.builder()
                .result(2)
                .msg("Oluvchi statusi faol xolatga keltirildi")
                .wsId(77L)
                .build();

        when(repository.restorePersonStatus(anyString())).thenReturn(oracleResult);
        when(repository.clobToString(responseClob)).thenReturn(oracleJsonResponse);
        when(objectMapper.readValue(oracleJsonResponse, PersonAbroadRestoreStatusResponseDto.class))
                .thenReturn(expectedResponse);

        // When
        PersonAbroadRestoreStatusResponseDto response = service.restoreStatus(requestDto);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getResult());
        assertEquals("Oluvchi statusi faol xolatga keltirildi", response.getMsg());
        assertEquals(77L, response.getWsId());

        verify(repository).restorePersonStatus(anyString());
    }

    @Test
    @DisplayName("Restore Status: Should return already active (result=1)")
    void restoreStatus_shouldReturnAlreadyActive() throws Exception {
        // Given
        String oracleJsonResponse = "{\"result\": 1, \"msg\": \"Pensiya oluvchilar ro'yhatida mavjud\", \"ws_id\": 77}";
        Clob responseClob = new SerialClob(oracleJsonResponse.toCharArray());

        Map<String, Object> oracleResult = new HashMap<>();
        oracleResult.put("RETURN", 1);
        oracleResult.put("O_Data", responseClob);

        PersonAbroadRestoreStatusResponseDto expectedResponse = PersonAbroadRestoreStatusResponseDto.builder()
                .result(1)
                .msg("Pensiya oluvchilar ro'yhatida mavjud")
                .wsId(77L)
                .build();

        when(repository.restorePersonStatus(anyString())).thenReturn(oracleResult);
        when(repository.clobToString(responseClob)).thenReturn(oracleJsonResponse);
        when(objectMapper.readValue(oracleJsonResponse, PersonAbroadRestoreStatusResponseDto.class))
                .thenReturn(expectedResponse);

        // When
        PersonAbroadRestoreStatusResponseDto response = service.restoreStatus(requestDto);

        // Then
        assertEquals(1, response.getResult());
    }

    @Test
    @DisplayName("Restore Status: Should return not arrived (result=3)")
    void restoreStatus_shouldReturnNotArrived() throws Exception {
        // Given
        String oracleJsonResponse = "{\"result\": 3, \"msg\": \"O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi\", \"ws_id\": 77}";
        Clob responseClob = new SerialClob(oracleJsonResponse.toCharArray());

        Map<String, Object> oracleResult = new HashMap<>();
        oracleResult.put("RETURN", 1);
        oracleResult.put("O_Data", responseClob);

        PersonAbroadRestoreStatusResponseDto expectedResponse = PersonAbroadRestoreStatusResponseDto.builder()
                .result(3)
                .msg("O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi")
                .wsId(77L)
                .build();

        when(repository.restorePersonStatus(anyString())).thenReturn(oracleResult);
        when(repository.clobToString(responseClob)).thenReturn(oracleJsonResponse);
        when(objectMapper.readValue(oracleJsonResponse, PersonAbroadRestoreStatusResponseDto.class))
                .thenReturn(expectedResponse);

        // When
        PersonAbroadRestoreStatusResponseDto response = service.restoreStatus(requestDto);

        // Then
        assertEquals(3, response.getResult());
    }

    @Test
    @DisplayName("Restore Status: Should return error when person not found (result=0)")
    void restoreStatus_shouldReturnErrorWhenPersonNotFound() throws Exception {
        // Given
        String oracleJsonResponse = "{\"result\": 0, \"msg\": \"Pensiya oluvchilar ro'yhatida mavjud emas\", \"ws_id\": 77}";
        Clob responseClob = new SerialClob(oracleJsonResponse.toCharArray());

        Map<String, Object> oracleResult = new HashMap<>();
        oracleResult.put("RETURN", 0);
        oracleResult.put("O_Data", responseClob);

        PersonAbroadRestoreStatusResponseDto expectedResponse = PersonAbroadRestoreStatusResponseDto.builder()
                .result(0)
                .msg("Pensiya oluvchilar ro'yhatida mavjud emas")
                .wsId(77L)
                .build();

        when(repository.restorePersonStatus(anyString())).thenReturn(oracleResult);
        when(repository.clobToString(responseClob)).thenReturn(oracleJsonResponse);
        when(objectMapper.readValue(oracleJsonResponse, PersonAbroadRestoreStatusResponseDto.class))
                .thenReturn(expectedResponse);

        // When
        PersonAbroadRestoreStatusResponseDto response = service.restoreStatus(requestDto);

        // Then
        assertEquals(0, response.getResult());
    }

    @Test
    @DisplayName("Restore Status: Should handle Oracle exception gracefully")
    void restoreStatus_shouldHandleOracleException() {
        // Given
        when(repository.restorePersonStatus(anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        PersonAbroadRestoreStatusResponseDto response = service.restoreStatus(requestDto);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getResult());
        assertTrue(response.getMsg().contains("Ma'lumotni qayta ishlashda xatolik"));
        assertEquals(77L, response.getWsId());
    }

    @Test
    @DisplayName("Should convert request to XML correctly")
    void shouldConvertRequestToXmlCorrectly() throws Exception {
        // This tests the XML conversion by verifying the format sent to Oracle
        String expectedXml = "<Data><ws_id>77</ws_id><pinfl>12345678901234</pinfl></Data>";

        String oracleJsonResponse = "{\"result\": 1, \"msg\": \"\", \"ws_id\": 77, \"status\": 1}";
        Clob responseClob = new SerialClob(oracleJsonResponse.toCharArray());

        Map<String, Object> oracleResult = new HashMap<>();
        oracleResult.put("RETURN", 1);
        oracleResult.put("O_Data", responseClob);

        PersonAbroadCheckStatusResponseDto expectedResponse = PersonAbroadCheckStatusResponseDto.builder()
                .result(1)
                .msg("")
                .wsId(77L)
                .status(1)
                .build();

        when(repository.checkPersonStatus(anyString())).thenReturn(oracleResult);
        when(repository.clobToString(responseClob)).thenReturn(oracleJsonResponse);
        when(objectMapper.readValue(oracleJsonResponse, PersonAbroadCheckStatusResponseDto.class))
                .thenReturn(expectedResponse);

        // When
        service.checkStatus(requestDto);

        // Then
        verify(repository).checkPersonStatus(expectedXml);
    }
}
