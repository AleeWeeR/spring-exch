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
import uz.fido.pfexchange.dto.mip.ChargeHistoryResponseDto;
import uz.fido.pfexchange.dto.mip.ChargeRequestDto;
import uz.fido.pfexchange.dto.mip.ChargeResponseDto;
import uz.fido.pfexchange.service.ChargeService;


/**
 * REST Controller for charge/retention operations
 * Pensionerlar qarzdorligi bo'yicha ma'lumot olish uchun REST kontroller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/charges")
@RequiredArgsConstructor
@Validated
@Tag(name = "Qarzdorlik Boshqaruvi", description = "Pensionerlarning qarzdorligi va ushlab qolishlari haqida ma'lumot olish API'lari")
public class ChargeController {

    private final ChargeService chargeService;

    /**
     * Endpoint 1: Shaxsning qarzdorligi mavjudligini tekshirish
     *
     * @param request ws_id va pinfl parametrlarini o'z ichiga olgan so'rov
     * @return Joriy qarzdorlik holati va tafsilotlari bilan ma'lumot
     */
    @PostMapping("/info")
    @PreAuthorize(value = "hasAnyAuthority('GET_CHARGE_INFO')")
    @Operation(
            summary = "Qarzdorlik ma'lumotini olish",
            description = "Shaxsning faol qarzdorligi mavjudligini tekshiradi. Joriy qarz balansini va tafsilotlarini qaytaradi."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Qarzdorlik ma'lumoti muvaffaqiyatli olindi",
                    content = @Content(schema = @Schema(implementation = ChargeResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri so'rov parametrlari (ws_id yoki pinfl xato)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ichki server xatosi"
            )
    })
    public ResponseEntity<ChargeResponseDto> getChargesInfo(@Valid @RequestBody ChargeRequestDto request) {
        log.info("Qarzdorlik ma'lumoti so'rovi qabul qilindi - ws_id: {}", request.getWsId());

        ChargeResponseDto response = chargeService.getChargesInfo(request);

        log.info("Qarzdorlik ma'lumoti olindi - natija: {}, xabar: {}", response.getResult(), response.getMsg());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 2: Shaxsning qarzdorlik tarixini olish
     *
     * @param request ws_id va pinfl parametrlarini o'z ichiga olgan so'rov
     * @return Davr bo'yicha ushlab qolish tafsilotlari bilan qarzdorlik tarixi
     */
    @PostMapping("/history")
    @PreAuthorize(value = "hasAnyAuthority('GET_CHARGE_HIST')")
    @Operation(
            summary = "Qarzdorlik tarixini olish",
            description = "Shaxsning qarzdorlik/ushlab qolish tarixini davr tafsilotlari va to'lov ma'lumotlari bilan olish."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Qarzdorlik tarixi muvaffaqiyatli olindi",
                    content = @Content(schema = @Schema(implementation = ChargeHistoryResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri so'rov parametrlari (ws_id yoki pinfl xato)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ichki server xatosi"
            )
    })
    public ResponseEntity<ChargeHistoryResponseDto> getChargedInfo(@Valid @RequestBody ChargeRequestDto request) {
        log.info("Qarzdorlik tarixi so'rovi qabul qilindi - ws_id: {}", request.getWsId());

        ChargeHistoryResponseDto response = chargeService.getChargedInfo(request);

        log.info("Qarzdorlik tarixi olindi - natija: {}, xabar: {}", response.getResult(), response.getMsg());

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
        return ResponseEntity.ok("Qarzdorlik API'si ishga tushgan va faol");
    }
}