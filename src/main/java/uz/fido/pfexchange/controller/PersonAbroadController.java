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
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusResponseDto;
import uz.fido.pfexchange.service.PersonAbroadService;

/**
 * REST Controller for person abroad status operations
 * Pensiya oluvchilar holatini tekshirish uchun REST kontroller
 *
 * Natija kodlari:
 * 0 - Pensiya oluvchilar ro'yhatida mavjud emas
 * 1 - Pensiya oluvchilar ro'yhatida mavjud
 * 2 - Oluvchi statusi faol xolatga keltirildi
 * 3 - O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi
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
     * Pensiya oluvchining holatini tekshirish va kerak bo'lsa faollashtirish
     *
     * @param request ws_id va pinfl parametrlarini o'z ichiga olgan so'rov
     * @return Holat kodi va ma'lumotlar bilan javob
     */
    @PostMapping("/status")
    @PreAuthorize(value = "hasAnyAuthority('GET_PERSON_ABROAD_STATUS')")
    @Operation(
            summary = "Pensiya oluvchi holatini tekshirish",
            description = "Shaxsning pensiya oluvchilar ro'yhatida mavjudligini tekshiradi va kerak bo'lsa faollashtiradi. " +
                    "Javobda natija kodi qaytariladi: " +
                    "0=Ro'yhatda yo'q, 1=Ro'yhatda mavjud, 2=Faol xolatga keltirildi, 3=Kirganlik aniqlanmadi"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Holat ma'lumoti muvaffaqiyatli olindi",
                    content = @Content(schema = @Schema(implementation = PersonAbroadStatusResponseDto.class))
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
    public ResponseEntity<PersonAbroadStatusResponseDto> checkStatus(@Valid @RequestBody PersonAbroadStatusRequestDto request) {
        log.info("Holat tekshiruvi so'rovi qabul qilindi - ws_id: {}, pinfl: {}",
                request.getData().getWsId(),
                request.getData().getPinfl()
        );

        PersonAbroadStatusResponseDto response = personAbroadService.checkStatus(request);

        log.info("Holat tekshiruvi yakunlandi - natija: {}, xabar: {}",
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
