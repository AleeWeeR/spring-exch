package uz.fido.pfexchange.controller.mip;

import static uz.fido.pfexchange.config.Authority.Codes.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import uz.fido.pfexchange.dto.abroad.PersonAbroadCheckStatusResponseDto;
import uz.fido.pfexchange.dto.abroad.PersonAbroadRestoreStatusResponseDto;
import uz.fido.pfexchange.dto.abroad.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.service.mip.MipPersonAbroadService;
import uz.fido.pfexchange.utils.validation.OrderedChecks;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mip/person-abroad")
@Tag(
        name = "Pensiya Oluvchi Holat Tekshiruvi",
        description = "Pensiya oluvchilar holatini tekshirish va faollashtirish API'lari")
public class MipPersonAbroadController {

    private final MipPersonAbroadService personAbroadService;

    @PostMapping("/check-status")
    @PreAuthorize("hasAuthority('" + GET_PERSON_ABROAD_STATUS + "')")
    @Operation(
            summary = "Pensiya oluvchi holatini tekshirish",
            description =
                    "Shaxsning pensiya oluvchilar ro'yhatidagi holatini tekshiradi. Natija kodlari:"
                        + " 0=Ro'yhatda yo'q, 1=Faol, 2=Nofaol (chet elda 3+ oy), 3=Nofaol (boshqa"
                        + " sabablar)")
    public ResponseEntity<PersonAbroadCheckStatusResponseDto> checkStatus(
            @Validated(OrderedChecks.class) @RequestBody PersonAbroadStatusRequestDto request) {
        return ResponseEntity.ok(personAbroadService.checkStatus(request));
    }

    @PostMapping("/restore-status")
    @PreAuthorize("hasAuthority('" + RESTORE_PERSON_ABROAD_STATUS + "')")
    @Operation(
            summary = "Pensiya oluvchi holatini tiklash",
            description =
                    "Chet eldan qaytgan shaxsni faollashtiradi. Natija kodlari: 0=Ro'yhatda yo'q,"
                        + " 1=Allaqachon faol, 2=Faollashtirildi, 3=Kirish aniqlanmadi")
    public ResponseEntity<PersonAbroadRestoreStatusResponseDto> restoreStatus(
            @Validated(OrderedChecks.class) @RequestBody PersonAbroadStatusRequestDto request) {
        return ResponseEntity.ok(personAbroadService.restoreStatus(request));
    }
}
