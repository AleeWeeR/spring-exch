package uz.fido.pfexchange.controller.mib;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import uz.fido.pfexchange.dto.mib.MibCancelDebtRequestDto;

import uz.fido.pfexchange.service.mib.MibCancelDebtService;

import static uz.fido.pfexchange.config.Authority.Codes.GET_PERSON_ABROAD_STATUS;
import static uz.fido.pfexchange.config.Authority.Codes.SEND_CANCEL_DEBT;

/**
 * REST Controller for debt cancellation operations
 * Qarzdorliklarni bekor qilish uchun REST kontroller
 *
 * This controller provides endpoint to cancel debts with MIB pension system
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mib/debt-cancellation")
@RequiredArgsConstructor
@Validated
@Tag(name = "Qarzdorlik Bekor Qilish", description = "MIB tizimidagi qarzdorliklarni bekor qilish API'lari")
public class MibCancelDebtController {

    private final MibCancelDebtService debtCancellationService;

    /**
     * Cancel debt for a specific external ID
     * Natija kodlari:
     * 0 - Qarzdorlikni bekor qilishda xatolik
     * 1 - Qarzdorlik muvaffaqiyatli bekor qilindi
     *
     */
    @PostMapping("/send-mib-cancel")
    @PreAuthorize("hasAuthority('" + SEND_CANCEL_DEBT + "')")
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
    public ResponseEntity<String> sendMibCancel(@Valid @RequestBody MibCancelDebtRequestDto payload) {
        log.info("MIB cancel debt request received - inventory_id: {}", payload.getInventoryId());

        String response = debtCancellationService.sendCancelDebtRequest(payload);

        log.info("MIB cancel debt response sent");

        // Return response as plain text or XML (matching JSP behavior)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(response);
    }


}