package uz.fido.pfexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.fido.pfexchange.dto.mib.DebtCancellationRequestDto;
import uz.fido.pfexchange.dto.mib.DebtCancellationResponseDto;
import uz.fido.pfexchange.service.DebtCancellationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DebtCancellationController
 */
@WebMvcTest(DebtCancellationController.class)
@DisplayName("Debt Cancellation Controller Tests")
class DebtCancellationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DebtCancellationService debtCancellationService;

    private DebtCancellationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new DebtCancellationRequestDto(123456L);
    }

    // ========================================================================
    // Cancel Debt Endpoint Tests
    // ========================================================================

    @Test
    @WithMockUser(authorities = "CANCEL_DEBT")
    @DisplayName("POST /cancel: Should successfully cancel debt")
    void cancelDebt_shouldSuccessfullyCancelDebt() throws Exception {
        // Given
        DebtCancellationResponseDto response = DebtCancellationResponseDto.builder()
            .result(1)
            .msg("Qarzdorlik muvaffaqiyatli bekor qilindi")
            .externalId(123456L)
            .isSent("Y")
            .isCancelled("Y")
            .build();

        when(debtCancellationService.cancelDebt(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/debt-cancellation/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(1))
            .andExpect(jsonPath("$.msg").value("Qarzdorlik muvaffaqiyatli bekor qilindi"))
            .andExpect(jsonPath("$.external_id").value(123456))
            .andExpect(jsonPath("$.is_sent").value("Y"))
            .andExpect(jsonPath("$.is_cancelled").value("Y"));
    }

    @Test
    @WithMockUser(authorities = "CANCEL_DEBT")
    @DisplayName("POST /cancel: Should return error when debt not found")
    void cancelDebt_shouldReturnErrorWhenDebtNotFound() throws Exception {
        // Given
        DebtCancellationResponseDto response = DebtCancellationResponseDto.builder()
            .result(0)
            .msg("Qarzdorlik bekor qilish so'rovi topilmadi")
            .externalId(123456L)
            .isSent("N")
            .isCancelled("N")
            .build();

        when(debtCancellationService.cancelDebt(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/debt-cancellation/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(0))
            .andExpect(jsonPath("$.msg").value("Qarzdorlik bekor qilish so'rovi topilmadi"))
            .andExpect(jsonPath("$.external_id").value(123456))
            .andExpect(jsonPath("$.is_sent").value("N"))
            .andExpect(jsonPath("$.is_cancelled").value("N"));
    }

    @Test
    @WithMockUser(authorities = "CANCEL_DEBT")
    @DisplayName("POST /cancel: Should return already cancelled status")
    void cancelDebt_shouldReturnAlreadyCancelledStatus() throws Exception {
        // Given
        DebtCancellationResponseDto response = DebtCancellationResponseDto.builder()
            .result(1)
            .msg("result_code: 7; result msg: Already cancelled")
            .externalId(123456L)
            .isSent("Y")
            .isCancelled("Y")
            .build();

        when(debtCancellationService.cancelDebt(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/debt-cancellation/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(1))
            .andExpect(jsonPath("$.external_id").value(123456))
            .andExpect(jsonPath("$.is_sent").value("Y"))
            .andExpect(jsonPath("$.is_cancelled").value("Y"));
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    @DisplayName("POST /cancel: Should return forbidden with wrong authority")
    void cancelDebt_shouldReturnForbiddenWithWrongAuthority() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/debt-cancellation/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /cancel: Should return unauthorized without authentication")
    void cancelDebt_shouldReturnUnauthorizedWithoutAuth() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/debt-cancellation/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CANCEL_DEBT")
    @DisplayName("POST /cancel: Should validate request body")
    void cancelDebt_shouldValidateRequestBody() throws Exception {
        // Given - invalid request with null external_id
        String invalidRequest = "{\"external_id\": null}";

        // When & Then
        mockMvc.perform(post("/api/v1/debt-cancellation/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());
    }

    // ========================================================================
    // Health Check Endpoint Tests
    // ========================================================================

    @Test
    @DisplayName("GET /health: Should return service health status")
    void healthCheck_shouldReturnServiceHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/debt-cancellation/health"))
            .andExpect(status().isOk())
            .andExpect(content().string("Qarzdorlik Bekor Qilish API'si ishga tushgan va faol"));
    }
}
