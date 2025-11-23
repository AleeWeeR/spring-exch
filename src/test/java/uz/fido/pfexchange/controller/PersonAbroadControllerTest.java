package uz.fido.pfexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uz.fido.pfexchange.dto.mip.PersonAbroadCheckStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadRestoreStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusDataDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.service.PersonAbroadService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PersonAbroadController
 */
@WebMvcTest(PersonAbroadController.class)
@DisplayName("Person Abroad Controller Tests")
class PersonAbroadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonAbroadService personAbroadService;

    private PersonAbroadStatusRequestDto requestDto;

    @BeforeEach
    void setUp() {
        PersonAbroadStatusDataDto dataDto = PersonAbroadStatusDataDto.builder()
                .wsId(77L)
                .pinfl("12345678901234")
                .build();

        requestDto = PersonAbroadStatusRequestDto.builder()
                .data(dataDto)
                .build();
    }

    // ========================================================================
    // Check Status Endpoint Tests
    // ========================================================================

    @Test
    @WithMockUser(authorities = "GET_PERSON_ABROAD_STATUS")
    @DisplayName("POST /check-status: Should return active person status")
    void checkStatus_shouldReturnActivePersonStatus() throws Exception {
        // Given
        PersonAbroadCheckStatusResponseDto response = PersonAbroadCheckStatusResponseDto.builder()
                .result(1)
                .msg("")
                .wsId(77L)
                .status(1)
                .build();

        when(personAbroadService.checkStatus(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/check-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1))
                .andExpect(jsonPath("$.msg").value(""))
                .andExpect(jsonPath("$.ws_id").value(77))
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    @WithMockUser(authorities = "GET_PERSON_ABROAD_STATUS")
    @DisplayName("POST /check-status: Should return abroad person status")
    void checkStatus_shouldReturnAbroadPersonStatus() throws Exception {
        // Given
        PersonAbroadCheckStatusResponseDto response = PersonAbroadCheckStatusResponseDto.builder()
                .result(1)
                .msg("")
                .wsId(77L)
                .status(2)
                .build();

        when(personAbroadService.checkStatus(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/check-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(2));
    }

    @Test
    @WithMockUser(authorities = "GET_PERSON_ABROAD_STATUS")
    @DisplayName("POST /check-status: Should return inactive person status")
    void checkStatus_shouldReturnInactivePersonStatus() throws Exception {
        // Given
        PersonAbroadCheckStatusResponseDto response = PersonAbroadCheckStatusResponseDto.builder()
                .result(1)
                .msg("")
                .wsId(77L)
                .status(3)
                .build();

        when(personAbroadService.checkStatus(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/check-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(3));
    }

    @Test
    @WithMockUser(authorities = "GET_PERSON_ABROAD_STATUS")
    @DisplayName("POST /check-status: Should return error when person not found")
    void checkStatus_shouldReturnErrorWhenPersonNotFound() throws Exception {
        // Given
        PersonAbroadCheckStatusResponseDto response = PersonAbroadCheckStatusResponseDto.builder()
                .result(0)
                .msg("Pensiya oluvchilar ro'yhatida mavjud emas")
                .wsId(77L)
                .status(null)
                .build();

        when(personAbroadService.checkStatus(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/check-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.msg").value("Pensiya oluvchilar ro'yhatida mavjud emas"));
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    @DisplayName("POST /check-status: Should return 403 without proper authority")
    void checkStatus_shouldReturn403WithoutProperAuthority() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/check-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "GET_PERSON_ABROAD_STATUS")
    @DisplayName("POST /check-status: Should return 400 for invalid PINFL")
    void checkStatus_shouldReturn400ForInvalidPinfl() throws Exception {
        // Given - Invalid PINFL (less than 14 digits)
        PersonAbroadStatusDataDto invalidData = PersonAbroadStatusDataDto.builder()
                .wsId(77L)
                .pinfl("123")
                .build();

        PersonAbroadStatusRequestDto invalidRequest = PersonAbroadStatusRequestDto.builder()
                .data(invalidData)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/check-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========================================================================
    // Restore Status Endpoint Tests
    // ========================================================================

    @Test
    @WithMockUser(authorities = "RESTORE_PERSON_ABROAD_STATUS")
    @DisplayName("POST /restore-status: Should restore person successfully")
    void restoreStatus_shouldRestorePersonSuccessfully() throws Exception {
        // Given
        PersonAbroadRestoreStatusResponseDto response = PersonAbroadRestoreStatusResponseDto.builder()
                .result(2)
                .msg("Oluvchi statusi faol xolatga keltirildi")
                .wsId(77L)
                .build();

        when(personAbroadService.restoreStatus(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/restore-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(2))
                .andExpect(jsonPath("$.msg").value("Oluvchi statusi faol xolatga keltirildi"))
                .andExpect(jsonPath("$.ws_id").value(77))
                .andExpect(jsonPath("$.status").doesNotExist()); // No status field in restore response
    }

    @Test
    @WithMockUser(authorities = "RESTORE_PERSON_ABROAD_STATUS")
    @DisplayName("POST /restore-status: Should return already active")
    void restoreStatus_shouldReturnAlreadyActive() throws Exception {
        // Given
        PersonAbroadRestoreStatusResponseDto response = PersonAbroadRestoreStatusResponseDto.builder()
                .result(1)
                .msg("Pensiya oluvchilar ro'yhatida mavjud")
                .wsId(77L)
                .build();

        when(personAbroadService.restoreStatus(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/restore-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1));
    }

    @Test
    @WithMockUser(authorities = "RESTORE_PERSON_ABROAD_STATUS")
    @DisplayName("POST /restore-status: Should return not arrived")
    void restoreStatus_shouldReturnNotArrived() throws Exception {
        // Given
        PersonAbroadRestoreStatusResponseDto response = PersonAbroadRestoreStatusResponseDto.builder()
                .result(3)
                .msg("O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi")
                .wsId(77L)
                .build();

        when(personAbroadService.restoreStatus(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/restore-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(3));
    }

    @Test
    @WithMockUser(authorities = "RESTORE_PERSON_ABROAD_STATUS")
    @DisplayName("POST /restore-status: Should return error when person not found")
    void restoreStatus_shouldReturnErrorWhenPersonNotFound() throws Exception {
        // Given
        PersonAbroadRestoreStatusResponseDto response = PersonAbroadRestoreStatusResponseDto.builder()
                .result(0)
                .msg("Pensiya oluvchilar ro'yhatida mavjud emas")
                .wsId(77L)
                .build();

        when(personAbroadService.restoreStatus(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/restore-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0));
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    @DisplayName("POST /restore-status: Should return 403 without proper authority")
    void restoreStatus_shouldReturn403WithoutProperAuthority() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/person-abroad/restore-status")
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
        mockMvc.perform(get("/api/v1/person-abroad/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Person Abroad Holat Tekshiruvi API'si ishga tushgan va faol"));
    }
}
