package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.fido.pfexchange.dto.mip.ChargeHistoryResponseDto;
import uz.fido.pfexchange.dto.mip.ChargeRequestDto;
import uz.fido.pfexchange.dto.mip.ChargeResponseDto;
import uz.fido.pfexchange.exceptioning.RestException;

import java.sql.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChargeServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Charge Service Tests")
class ChargeServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Connection connection;

    @Mock
    private CallableStatement callableStatement;

    @Mock
    private Clob clob;

    @InjectMocks
    private ChargeServiceImpl service;

    private ChargeRequestDto requestDto;

    @BeforeEach
    void setUp() throws SQLException {
        requestDto = ChargeRequestDto.builder()
                .wsId(77L)
                .pinfl("12345678901234")
                .build();

        // Setup basic connection mock
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
    }

    // ========================================================================
    // Get Charges Info Tests
    // ========================================================================

    @Test
    @DisplayName("Get Charges Info: Should return has debt (result=1)")
    void getChargesInfo_shouldReturnHasDebt() throws Exception {
        // Given
        String jsonResponse = "{\"result\": 1, \"msg\": \"Qarzdorlik mavjud\", \"ws_id\": 77, \"fio\": \"Test User\", \"retention\": []}";

        ChargeResponseDto expectedResponse = ChargeResponseDto.builder()
                .result(1)
                .msg("Qarzdorlik mavjud")
                .wsId(77L)
                .fio("Test User")
                .retention(Arrays.asList())
                .build();

        setupMockForGetChargesInfo(1, jsonResponse, expectedResponse);

        // When
        ChargeResponseDto response = service.getChargesInfo(requestDto);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getResult());
        assertEquals("Qarzdorlik mavjud", response.getMsg());
        assertEquals(77L, response.getWsId());
        assertEquals("Test User", response.getFio());

        verify(callableStatement).registerOutParameter(1, Types.NUMERIC);
        verify(callableStatement).registerOutParameter(2, Types.CLOB);
        verify(callableStatement).setString(3, "<Data><ws_id>77</ws_id><pinfl>12345678901234</pinfl></Data>");
        verify(callableStatement).execute();
    }

    @Test
    @DisplayName("Get Charges Info: Should return no debt (result=2)")
    void getChargesInfo_shouldReturnNoDebt() throws Exception {
        // Given
        String jsonResponse = "{\"result\": 2, \"msg\": \"Qarzdorlik mavjud emas\", \"ws_id\": 77, \"fio\": \"Test User\", \"retention\": []}";

        ChargeResponseDto expectedResponse = ChargeResponseDto.builder()
                .result(2)
                .msg("Qarzdorlik mavjud emas")
                .wsId(77L)
                .fio("Test User")
                .retention(Arrays.asList())
                .build();

        setupMockForGetChargesInfo(1, jsonResponse, expectedResponse);

        // When
        ChargeResponseDto response = service.getChargesInfo(requestDto);

        // Then
        assertEquals(2, response.getResult());
        assertEquals("Qarzdorlik mavjud emas", response.getMsg());
    }

    @Test
    @DisplayName("Get Charges Info: Should return person not found (result=0)")
    void getChargesInfo_shouldReturnPersonNotFound() throws Exception {
        // Given
        String jsonResponse = "{\"result\": 0, \"msg\": \"Pensiya oluvchilar ro'yhatida mavjud emas\", \"ws_id\": 77}";

        ChargeResponseDto expectedResponse = ChargeResponseDto.builder()
                .result(0)
                .msg("Pensiya oluvchilar ro'yhatida mavjud emas")
                .wsId(77L)
                .build();

        setupMockForGetChargesInfo(0, jsonResponse, expectedResponse);

        // When
        ChargeResponseDto response = service.getChargesInfo(requestDto);

        // Then
        assertEquals(0, response.getResult());
        assertNotNull(response.getMsg());
    }

    @Test
    @DisplayName("Get Charges Info: Should handle database exception")
    void getChargesInfo_shouldHandleDatabaseException() throws SQLException {
        // Given
        when(jdbcTemplate.execute(any(org.springframework.jdbc.core.ConnectionCallback.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RestException.class, () -> service.getChargesInfo(requestDto));
    }

    // ========================================================================
    // Get Charged Info Tests (History)
    // ========================================================================

    @Test
    @DisplayName("Get Charged Info: Should return history with debt (result=1)")
    void getChargedInfo_shouldReturnHistoryWithDebt() throws Exception {
        // Given
        String jsonResponse = "{\"result\": 1, \"msg\": \"Qarzdorlik mavjud\", \"ws_id\": 77, \"fio\": \"Test User\", \"retention\": []}";

        ChargeHistoryResponseDto expectedResponse = ChargeHistoryResponseDto.builder()
                .result(1)
                .msg("Qarzdorlik mavjud")
                .wsId(77L)
                .fio("Test User")
                .retention(Arrays.asList())
                .build();

        setupMockForGetChargedInfo(1, jsonResponse, expectedResponse);

        // When
        ChargeHistoryResponseDto response = service.getChargedInfo(requestDto);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getResult());
        assertEquals("Qarzdorlik mavjud", response.getMsg());
        assertEquals(77L, response.getWsId());
        assertEquals("Test User", response.getFio());

        verify(callableStatement).registerOutParameter(1, Types.NUMERIC);
        verify(callableStatement).registerOutParameter(2, Types.CLOB);
        verify(callableStatement).setString(3, "<Data><ws_id>77</ws_id><pinfl>12345678901234</pinfl></Data>");
        verify(callableStatement).execute();
    }

    @Test
    @DisplayName("Get Charged Info: Should return history without debt (result=2)")
    void getChargedInfo_shouldReturnHistoryWithoutDebt() throws Exception {
        // Given
        String jsonResponse = "{\"result\": 2, \"msg\": \"Qarzdorlik mavjud emas\", \"ws_id\": 77, \"fio\": \"Test User\", \"retention\": []}";

        ChargeHistoryResponseDto expectedResponse = ChargeHistoryResponseDto.builder()
                .result(2)
                .msg("Qarzdorlik mavjud emas")
                .wsId(77L)
                .fio("Test User")
                .retention(Arrays.asList())
                .build();

        setupMockForGetChargedInfo(1, jsonResponse, expectedResponse);

        // When
        ChargeHistoryResponseDto response = service.getChargedInfo(requestDto);

        // Then
        assertEquals(2, response.getResult());
        assertEquals("Qarzdorlik mavjud emas", response.getMsg());
    }

    @Test
    @DisplayName("Get Charged Info: Should return person not found (result=0)")
    void getChargedInfo_shouldReturnPersonNotFound() throws Exception {
        // Given
        String jsonResponse = "{\"result\": 0, \"msg\": \"Pensiya oluvchilar ro'yhatida mavjud emas\", \"ws_id\": 77}";

        ChargeHistoryResponseDto expectedResponse = ChargeHistoryResponseDto.builder()
                .result(0)
                .msg("Pensiya oluvchilar ro'yhatida mavjud emas")
                .wsId(77L)
                .build();

        setupMockForGetChargedInfo(0, jsonResponse, expectedResponse);

        // When
        ChargeHistoryResponseDto response = service.getChargedInfo(requestDto);

        // Then
        assertEquals(0, response.getResult());
        assertNotNull(response.getMsg());
    }

    @Test
    @DisplayName("Get Charged Info: Should handle database exception")
    void getChargedInfo_shouldHandleDatabaseException() throws SQLException {
        // Given
        when(jdbcTemplate.execute(any(org.springframework.jdbc.core.ConnectionCallback.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RestException.class, () -> service.getChargedInfo(requestDto));
    }

    @Test
    @DisplayName("Should build correct XML input format")
    void shouldBuildCorrectXmlInputFormat() throws Exception {
        // This test verifies that XML is correctly formatted
        String expectedXml = "<Data><ws_id>77</ws_id><pinfl>12345678901234</pinfl></Data>";

        String jsonResponse = "{\"result\": 1, \"msg\": \"Test\", \"ws_id\": 77}";
        ChargeResponseDto expectedResponse = ChargeResponseDto.builder()
                .result(1)
                .msg("Test")
                .wsId(77L)
                .build();

        setupMockForGetChargesInfo(1, jsonResponse, expectedResponse);

        // When
        service.getChargesInfo(requestDto);

        // Then
        verify(callableStatement).setString(3, expectedXml);
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private void setupMockForGetChargesInfo(int returnValue, String jsonResponse, ChargeResponseDto expectedResponse) throws Exception {
        when(jdbcTemplate.execute(any(org.springframework.jdbc.core.ConnectionCallback.class)))
                .thenAnswer(invocation -> {
                    org.springframework.jdbc.core.ConnectionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInConnection(connection);
                });

        when(callableStatement.getInt(1)).thenReturn(returnValue);
        when(callableStatement.getClob(2)).thenReturn(clob);
        when(clob.getCharacterStream()).thenReturn(new java.io.StringReader(jsonResponse));
        when(objectMapper.readValue(jsonResponse, ChargeResponseDto.class)).thenReturn(expectedResponse);
    }

    private void setupMockForGetChargedInfo(int returnValue, String jsonResponse, ChargeHistoryResponseDto expectedResponse) throws Exception {
        when(jdbcTemplate.execute(any(org.springframework.jdbc.core.ConnectionCallback.class)))
                .thenAnswer(invocation -> {
                    org.springframework.jdbc.core.ConnectionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInConnection(connection);
                });

        when(callableStatement.getInt(1)).thenReturn(returnValue);
        when(callableStatement.getClob(2)).thenReturn(clob);
        when(clob.getCharacterStream()).thenReturn(new java.io.StringReader(jsonResponse));
        when(objectMapper.readValue(jsonResponse, ChargeHistoryResponseDto.class)).thenReturn(expectedResponse);
    }
}
