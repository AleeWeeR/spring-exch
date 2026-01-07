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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.mib.DebtCancellationRequestDto;
import uz.fido.pfexchange.dto.mib.DebtCancellationResponseDto;
import uz.fido.pfexchange.service.DebtCancellationService;

/**
 * REST Controller for debt cancellation operations
 * Qarzdorliklarni bekor qilish uchun REST kontroller
 *
 * This controller provides endpoint to cancel debts with MIB pension system
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/debt-cancellation")
@RequiredArgsConstructor
@Validated
@Tag(name = "Qarzdorlik Bekor Qilish", description = "MIB tizimidagi qarzdorliklarni bekor qilish API'lari")
public class DebtCancellationController {

    private final DebtCancellationService debtCancellationService;

    /**
     * Cancel debt for a specific external ID
     *
     * Natija kodlari:
     * 0 - Qarzdorlikni bekor qilishda xatolik
     * 1 - Qarzdorlik muvaffaqiyatli bekor qilindi
     *
     * @param request external_id parametrini o'z ichiga olgan so'rov
     * @return Bekor qilish natijasi
     */
    @PostMapping("/cancel")
    @PreAuthorize(value = "hasAnyAuthority('CANCEL_DEBT')")
    @Operation(
        summary = "Qarzdorlikni bekor qilish",
        description = "MIB pension tizimidagi qarzdorlikni bekor qilish so'rovini yuboradi. " +
            "Javobda natija kodi qaytariladi: " +
            "0=Xatolik, 1=Muvaffaqiyatli bekor qilindi"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bekor qilish so'rovi muvaffaqiyatli qayta ishlandi",
            content = @Content(schema = @Schema(implementation = DebtCancellationResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Noto'g'ri so'rov parametrlari (external_id xato)"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Ichki server xatosi"
        ),
        @ApiResponse(
            responseCode = "503",
            description = "MIB servisi mavjud emas"
        )
    })
    public ResponseEntity<DebtCancellationResponseDto> cancelDebt(
        @Valid @RequestBody DebtCancellationRequestDto request
    ) {
        log.info("Cancel debt request received - external_id: {}", request.getExternalId());

        DebtCancellationResponseDto response = debtCancellationService.cancelDebt(request);

        log.info("Cancel debt completed - result: {}, is_cancelled: {}",
            response.getResult(), response.getIsCancelled());

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint - Servis holatini tekshirish
     */
    @GetMapping("/health")
    @Operation(
        summary = "Servis holatini tekshirish",
        description = "API servisi ishga tushganligini va faolligini tekshirish"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Servis faol ishlayapti",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Qarzdorlik Bekor Qilish API'si ishga tushgan va faol");
    }
}
