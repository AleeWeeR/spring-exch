package uz.fido.pfexchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.mib.MibCancelDebtPayloadDto;
import uz.fido.pfexchange.service.MibCancelDebtService;

/**
 * REST Controller for MIB debt cancellation API proxy
 * This replaces the JSP sendMibCancel.jsp endpoint
 *
 * The PL/SQL function calls this endpoint which then forwards the request to MIB.
 * All business logic is handled in the PL/SQL function.
 */
@Slf4j
@RestController
@RequestMapping("/pf/mib")
@RequiredArgsConstructor
@Validated
@Tag(name = "MIB Qarzdorlik Bekor Qilish", description = "MIB pension API proxy (replaces JSP)")
public class MibCancelDebtController {

    private final MibCancelDebtService mibCancelDebtService;

    /**
     * Send cancel debt request to MIB pension API
     * This endpoint is called by PL/SQL function Task_Data_Send_Cancel_Debt
     *
     * Request body format (built by PL/SQL):
     * {
     *   "inventory_id": 123456,
     *   "fio_performer": "Name",
     *   "phone_performer": "123456789",
     *   "reason_id": 1,
     *   "reason_name": "Reason"
     * }
     *
     * Response:
     * - On success: JSON from MIB API
     * - On error: XML with result_code and result_message
     *
     * @param payload The cancellation payload (built by PL/SQL function)
     * @return Response from MIB API as string (JSON or XML)
     */
    @PostMapping("/sendMibCancel")
    @Operation(
        summary = "MIB pension API ga qarzdorlikni bekor qilish so'rovini yuborish",
        description = "Bu endpoint JSP sendMibCancel.jsp ni almashtiradi. " +
            "PL/SQL funksiyasi bu endpointni chaqiradi va u MIB ga so'rovni yo'naltiradi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "So'rov muvaffaqiyatli yuborildi (MIB javobini qaytaradi)",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Noto'g'ri so'rov parametrlari"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Ichki server xatosi"
        )
    })
    public ResponseEntity<String> sendMibCancel(@Valid @RequestBody MibCancelDebtPayloadDto payload) {
        log.info("MIB cancel debt request received - inventory_id: {}", payload.getInventoryId());

        String response = mibCancelDebtService.sendCancelDebtRequest(payload);

        log.info("MIB cancel debt response sent");

        // Return response as plain text or XML (matching JSP behavior)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(
        summary = "Servis holatini tekshirish",
        description = "API servisi ishga tushganligini va faolligini tekshirish"
    )
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("MIB Cancel Debt Proxy API ishga tushgan va faol");
    }
}
