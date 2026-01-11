package uz.fido.pfexchange.controller.mip;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeChangeRequestDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeChangeResponseDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeRequestDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeResponseDto;
import uz.fido.pfexchange.service.mip.MipPayTypeService;
import uz.fido.pfexchange.utils.ResponseBuilder;
import uz.fido.pfexchange.utils.validation.OrderedChecks;

import static uz.fido.pfexchange.config.Authority.Codes.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pf/mip/pay-type")
@Tag(
    name = "MIP To'lov Turi",
    description = "Pensioner to'lov turini boshqarish API'lari (karta, bank, pochta va h.k.)"
)
public class MipPayTypeController {

    private final MipPayTypeService mipPayTypeService;

    @PostMapping("/info")
    @PreAuthorize("hasAuthority('" + MIP_PAY_TYPE_INFO + "')")
    @Operation(
        summary = "To'lov turi ma'lumotini olish",
        description = "Pensionerning joriy to'lov turi va tafsilotlarini qaytaradi."
    )
    public ResponseEntity<ResponseWrapperDto<MipPayTypeResponseDto>> payTypeInfo(
            @Validated(OrderedChecks.class) @RequestBody MipPayTypeRequestDto requestDto) {
        return ResponseBuilder.ok(mipPayTypeService.payTypeInfo(requestDto));
    }

    @PostMapping("/change")
    @PreAuthorize("hasAuthority('" + MIP_PAY_TYPE_CHANGE + "')")
    @Operation(
        summary = "To'lov turini o'zgartirish",
        description = "Pensionerning to'lov turini yangilaydi (masalan, kartadan bankka o'tkazish)."
    )
    public ResponseEntity<ResponseWrapperDto<MipPayTypeChangeResponseDto>> payTypeChange(
            @Validated(OrderedChecks.class) @RequestBody MipPayTypeChangeRequestDto requestDto) {
        return ResponseBuilder.ok(mipPayTypeService.payTypeChange(requestDto));
    }
}