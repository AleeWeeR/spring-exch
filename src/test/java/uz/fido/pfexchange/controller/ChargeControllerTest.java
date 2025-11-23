package uz.fido.pfexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uz.fido.pfexchange.dto.mip.ChargeHistoryResponseDto;
import uz.fido.pfexchange.dto.mip.ChargeRequestDto;
import uz.fido.pfexchange.dto.mip.ChargeResponseDto;
import uz.fido.pfexchange.service.ChargeService;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ChargeController
 */
@WebMvcTest(ChargeController.class)
@DisplayName("Charge Controller Tests")
class ChargeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChargeService chargeService;

    private ChargeRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = ChargeRequestDto.builder()
                .wsId(77L)
                .pinfl("12345678901234")
                .build();
    }

    // ========================================================================
    // Get Charges Info Endpoint Tests
    // ========================================================================

    @Test
    @WithMockUser(authorities = "GET_CHARGE_INFO")
    @DisplayName("POST /info: Should return charges info with debt")
    void getChargesInfo_shouldReturnChargesInfoWithDebt() throws Exception {
        // Given
        ChargeResponseDto response = ChargeResponseDto.builder()
                .result(1)
                .msg("Qarzdorlik mavjud")
                .wsId(77L)
                .fio("Test User")
                .retention(Collections.emptyList())
                .build();

        when(chargeService.getChargesInfo(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/charges/info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1))
                .andExpect(jsonPath("$.msg").value("Qarzdorlik mavjud"))
                .andExpect(jsonPath("$.ws_id").value(77))
                .andExpect(jsonPath("$.fio").value("Test User"))
                .andExpect(jsonPath("$.retention").isArray());
    }

    @Test
    @WithMockUser(authorities = "GET_CHARGE_INFO")
    @DisplayName("POST /info: Should return no debt")
    void getChargesInfo_shouldReturnNoDebt() throws Exception {
        // Given
        ChargeResponseDto response = ChargeResponseDto.builder()
                .result(2)
                .msg("Qarzdorlik mavjud emas")
                .wsId(77L)
                .fio("Test User")
                .retention(Collections.emptyList())
                .build();

        when(chargeService.getChargesInfo(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/charges/info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(2))
                .andExpect(jsonPath("$.msg").value("Qarzdorlik mavjud emas"));
    }

    @Test
    @WithMockUser(authorities = "GET_CHARGE_INFO")
    @DisplayName("POST /info: Should return person not found")
    void getChargesInfo_shouldReturnPersonNotFound() throws Exception {
        // Given
        ChargeResponseDto response = ChargeResponseDto.builder()
                .result(0)
                .msg("Pensiya oluvchilar ro'yhatida mavjud emas")
                .wsId(77L)
                .build();

        when(chargeService.getChargesInfo(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/charges/info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.msg").value("Pensiya oluvchilar ro'yhatida mavjud emas"));
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    @DisplayName("POST /info: Should return 403 without proper authority")
    void getChargesInfo_shouldReturn403WithoutProperAuthority() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/charges/info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "GET_CHARGE_INFO")
    @DisplayName("POST /info: Should return 400 for invalid PINFL")
    void getChargesInfo_shouldReturn400ForInvalidPinfl() throws Exception {
        // Given - Invalid PINFL (less than 14 digits)
        ChargeRequestDto invalidRequest = ChargeRequestDto.builder()
                .wsId(77L)
                .pinfl("123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/charges/info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "GET_CHARGE_INFO")
    @DisplayName("POST /info: Should return 400 for null ws_id")
    void getChargesInfo_shouldReturn400ForNullWsId() throws Exception {
        // Given - Null ws_id
        ChargeRequestDto invalidRequest = ChargeRequestDto.builder()
                .wsId(null)
                .pinfl("12345678901234")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/charges/info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========================================================================
    // Get Charged Info (History) Endpoint Tests
    // ========================================================================

    @Test
    @WithMockUser(authorities = "GET_CHARGE_HIST")
    @DisplayName("POST /history: Should return charge history with debt")
    void getChargedInfo_shouldReturnChargeHistoryWithDebt() throws Exception {
        // Given
        ChargeHistoryResponseDto response = ChargeHistoryResponseDto.builder()
                .result(1)
                .msg("Qarzdorlik mavjud")
                .wsId(77L)
                .fio("Test User")
                .retention(Collections.emptyList())
                .build();

        when(chargeService.getChargedInfo(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/charges/history")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1))
                .andExpect(jsonPath("$.msg").value("Qarzdorlik mavjud"))
                .andExpect(jsonPath("$.ws_id").value(77))
                .andExpect(jsonPath("$.fio").value("Test User"))
                .andExpect(jsonPath("$.retention").isArray());
    }

    @Test
    @WithMockUser(authorities = "GET_CHARGE_HIST")
    @DisplayName("POST /history: Should return history without debt")
    void getChargedInfo_shouldReturnHistoryWithoutDebt() throws Exception {
        // Given
        ChargeHistoryResponseDto response = ChargeHistoryResponseDto.builder()
                .result(2)
                .msg("Qarzdorlik mavjud emas")
                .wsId(77L)
                .fio("Test User")
                .retention(Collections.emptyList())
                .build();

        when(chargeService.getChargedInfo(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/charges/history")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(2))
                .andExpect(jsonPath("$.msg").value("Qarzdorlik mavjud emas"));
    }

    @Test
    @WithMockUser(authorities = "GET_CHARGE_HIST")
    @DisplayName("POST /history: Should return person not found")
    void getChargedInfo_shouldReturnPersonNotFound() throws Exception {
        // Given
        ChargeHistoryResponseDto response = ChargeHistoryResponseDto.builder()
                .result(0)
                .msg("Pensiya oluvchilar ro'yhatida mavjud emas")
                .wsId(77L)
                .build();

        when(chargeService.getChargedInfo(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/charges/history")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.msg").value("Pensiya oluvchilar ro'yhatida mavjud emas"));
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    @DisplayName("POST /history: Should return 403 without proper authority")
    void getChargedInfo_shouldReturn403WithoutProperAuthority() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/charges/history")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    // ========================================================================
    // Health Check Tests
    // ========================================================================

    @Test
    @DisplayName("GET /health: Should return service health status")
    void healthCheck_shouldReturnServiceHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/charges/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Qarzdorlik API'si ishga tushgan va faol"));
    }
}
