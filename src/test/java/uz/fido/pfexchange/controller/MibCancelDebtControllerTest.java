package uz.fido.pfexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.fido.pfexchange.dto.mib.MibCancelDebtPayloadDto;
import uz.fido.pfexchange.service.MibCancelDebtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MibCancelDebtController
 */
@WebMvcTest(MibCancelDebtController.class)
@DisplayName("MIB Cancel Debt Controller Tests")
class MibCancelDebtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MibCancelDebtService mibCancelDebtService;

    private MibCancelDebtPayloadDto payload;

    @BeforeEach
    void setUp() {
        payload = MibCancelDebtPayloadDto.builder()
            .inventoryId(123456L)
            .fioPerformer("Test User")
            .phonePerformer("998901234567")
            .reasonId(1)
            .reasonName("Test Reason")
            .build();
    }

    // ========================================================================
    // Send MIB Cancel Endpoint Tests
    // ========================================================================

    @Test
    @DisplayName("POST /sendMibCancel: Should successfully forward request to MIB")
    void sendMibCancel_shouldSuccessfullyForwardRequest() throws Exception {
        // Given
        String mibResponse = "{\"result_code\": 0, \"result_message\": \"Success\"}";
        when(mibCancelDebtService.sendCancelDebtRequest(any())).thenReturn(mibResponse);

        // When & Then
        mockMvc.perform(post("/pf/mib/sendMibCancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(content().string(mibResponse));
    }

    @Test
    @DisplayName("POST /sendMibCancel: Should return error XML on failure")
    void sendMibCancel_shouldReturnErrorXmlOnFailure() throws Exception {
        // Given
        String errorXml = "<result_code>1</result_code><result_message>Network error</result_message>";
        when(mibCancelDebtService.sendCancelDebtRequest(any())).thenReturn(errorXml);

        // When & Then
        mockMvc.perform(post("/pf/mib/sendMibCancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(content().string(errorXml));
    }

    @Test
    @DisplayName("POST /sendMibCancel: Should validate request body")
    void sendMibCancel_shouldValidateRequestBody() throws Exception {
        // Given - invalid request with null inventory_id
        String invalidRequest = "{\"inventory_id\": null, \"fio_performer\": \"Test\"}";

        // When & Then
        mockMvc.perform(post("/pf/mib/sendMibCancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /sendMibCancel: Should accept valid payload")
    void sendMibCancel_shouldAcceptValidPayload() throws Exception {
        // Given
        String successResponse = "{\"result_code\": 0, \"result_message\": \"Success\"}";
        when(mibCancelDebtService.sendCancelDebtRequest(any())).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/pf/mib/sendMibCancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andExpect(content().string(successResponse));
    }

    // ========================================================================
    // Health Check Endpoint Tests
    // ========================================================================

    @Test
    @DisplayName("GET /health: Should return service health status")
    void healthCheck_shouldReturnServiceHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/pf/mib/health"))
            .andExpect(status().isOk())
            .andExpect(content().string("MIB Cancel Debt Proxy API ishga tushgan va faol"));
    }
}
