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
import uz.fido.pfexchange.dto.mip.PersonAbroadCheckStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadRestoreStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.service.PersonAbroadService;

/**
 * REST Controller for person abroad status operations
 * Pensiya oluvchilar holatini tekshirish uchun REST kontroller
 *
 * TWO ENDPOINTS:
 * 1. /check-status - Just check status (no restoration)
 * 2. /restore-status - Check arrival and restore if needed
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/person-abroad")
@RequiredArgsConstructor
@Validated
@Tag(name = "Pensiya Oluvchi Holat Tekshiruvi", description = "Pensiya oluvchilar holatini tekshirish va faollashtirish API'lari")
public class PersonAbroadController {

    private final PersonAbroadService personAbroadService;

    /**
     * ENDPOINT 1: Just check person status (no restoration)
     *
     * Natija kodlari:
     * 0 - Pensiya oluvchilar ro'yhatida mavjud emas
     * 1 - Faol (active)
     * 2 - Nofaol, close_desc=11 (chet elda 3 oydan ortiq)
     * 3 - Nofaol, boshqa sabablar bilan
     *
     * @param request ws_id va pinfl parametrlarini o'z ichiga olgan so'rov
     * @return Holat kodi va ma'lumotlar bilan javob
     */
    @PostMapping("/check-status")
    @PreAuthorize(value = "hasAnyAuthority('GET_PERSON_ABROAD_STATUS')")
    @Operation(
            summary = "Pensiya oluvchi holatini tekshirish (faollashtirishsiz)",
            description = "Shaxsning pensiya oluvchilar ro'yhatida mavjudligini va holatini tekshiradi. " +
                    "Javobda natija kodi qaytariladi: " +
                    "0=Ro'yhatda yo'q, 1=Faol, 2=Nofaol (chet elda), 3=Nofaol (boshqa sabablar)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Holat ma'lumoti muvaffaqiyatli olindi",
                    content = @Content(schema = @Schema(implementation = PersonAbroadCheckStatusResponseDto.class))
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
    public ResponseEntity<PersonAbroadCheckStatusResponseDto> checkStatus(@Valid @RequestBody PersonAbroadStatusRequestDto request) {
        log.info("Check status request received - ws_id: {}, pinfl: {}",
                request.getData().getWsId(),
                request.getData().getPinfl()
        );

        PersonAbroadCheckStatusResponseDto response = personAbroadService.checkStatus(request);

        log.info("Check status completed - result: {}, status: {}", response.getResult(), response.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * ENDPOINT 2: Check arrival and restore person if needed
     *
     * Natija kodlari:
     * 0 - Pensiya oluvchilar ro'yhatida mavjud emas
     * 1 - Pensiya oluvchilar ro'yhatida mavjud (already active)
     * 2 - Oluvchi statusi faol xolatga keltirildi (restored)
     * 3 - O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi
     *
     * @param request ws_id va pinfl parametrlarini o'z ichiga olgan so'rov
     * @return Holat kodi va ma'lumotlar bilan javob
     */
    @PostMapping("/restore-status")
    @PreAuthorize(value = "hasAnyAuthority('RESTORE_PERSON_ABROAD_STATUS')")
    @Operation(
            summary = "Pensiya oluvchi holatini tiklash",
            description = "Chet elda bo'lgan shaxsning qaytib kelganligini tekshiradi va faollashtiradi. " +
                    "Javobda natija kodi qaytariladi: " +
                    "0=Ro'yhatda yo'q, 1=Faol, 2=Faol xolatga keltirildi, 3=Kirganlik aniqlanmadi"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tiklash ma'lumoti muvaffaqiyatli olindi",
                    content = @Content(schema = @Schema(implementation = PersonAbroadRestoreStatusResponseDto.class))
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
    public ResponseEntity<PersonAbroadRestoreStatusResponseDto> restoreStatus(@Valid @RequestBody PersonAbroadStatusRequestDto request) {
        log.info("Restore status request received - ws_id: {}, pinfl: {}",
                request.getData().getWsId(),
                request.getData().getPinfl()
        );

        PersonAbroadRestoreStatusResponseDto response = personAbroadService.restoreStatus(request);

        log.info("Restore status completed - result: {}, message: {}",
                response.getResult(),
                response.getMsg()
        );

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
        return ResponseEntity.ok("Person Abroad Holat Tekshiruvi API'si ishga tushgan va faol");
    }
}
