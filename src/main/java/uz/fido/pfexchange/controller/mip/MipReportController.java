package uz.fido.pfexchange.controller.mip;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.mip.report.MipReportRequestDto;
import uz.fido.pfexchange.dto.mip.report.MipReportResponseDto;
import uz.fido.pfexchange.service.mip.MipReportService;
import uz.fido.pfexchange.utils.ResponseBuilder;
import static uz.fido.pfexchange.config.Authority.Codes.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pf/mip/report")
@Tag(
    name = "MIP Hisobotlar",
    description = "Pensiya bo'yicha hisobotlar va ma'lumotlarni olish API'lari"
)
public class MipReportController {

    private final MipReportService mipReportService;

    @PostMapping("/pension-info")
    @PreAuthorize("hasAuthority('" + GET_MIP_INFO + "')")
    @Operation(
        summary = "Pensiya ma'lumotini olish",
        description = "Shaxsning pensiya holati, miqdori va to'lov tafsilotlarini qaytaradi."
    )
    public ResponseEntity<ResponseWrapperDto<MipReportResponseDto>> pensionInfo(
            @Valid @RequestBody MipReportRequestDto requestDto) {
        return ResponseBuilder.ok(mipReportService.pensionInfo(requestDto));
    }
}