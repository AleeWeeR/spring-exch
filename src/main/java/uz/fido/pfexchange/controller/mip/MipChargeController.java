package uz.fido.pfexchange.controller.mip;

import static uz.fido.pfexchange.config.Authority.Codes.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import uz.fido.pfexchange.dto.mip.charge.MipChargeHistoryResponseDto;
import uz.fido.pfexchange.dto.mip.charge.MipChargeRequestDto;
import uz.fido.pfexchange.dto.mip.charge.MipChargeResponseDto;
import uz.fido.pfexchange.service.mip.MipChargeService;
import uz.fido.pfexchange.utils.validation.OrderedChecks;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mip/charges")
@Tag(
        name = "Qarzdorlik Boshqaruvi",
        description =
                "Pensionerlarning qarzdorligi va ushlab qolishlari haqida ma'lumot olish API'lari")
public class MipChargeController {

    private final MipChargeService chargeService;

    @PostMapping("/info")
    @PreAuthorize("hasAuthority('" + GET_CHARGE_INFO + "')")
    @Operation(
            summary = "Qarzdorlik ma'lumotini olish",
            description =
                    "Shaxsning faol qarzdorligi mavjudligini tekshiradi. Joriy qarz balansini va"
                        + " tafsilotlarini qaytaradi.")
    public ResponseEntity<MipChargeResponseDto> getChargesInfo(
            @Validated(OrderedChecks.class) @RequestBody MipChargeRequestDto request) {
        MipChargeResponseDto response = chargeService.getChargesInfo(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/history")
    @PreAuthorize("hasAuthority('" + GET_CHARGE_HIST + "')")
    @Operation(
            summary = "Qarzdorlik tarixini olish",
            description =
                    "Shaxsning qarzdorlik/ushlab qolish tarixini davr tafsilotlari va to'lov"
                        + " ma'lumotlari bilan olish.")
    public ResponseEntity<MipChargeHistoryResponseDto> getChargedInfo(
            @Validated(OrderedChecks.class) @RequestBody MipChargeRequestDto request) {
        MipChargeHistoryResponseDto response = chargeService.getChargedInfo(request);

        return ResponseEntity.ok(response);
    }
}
