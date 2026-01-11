package uz.fido.pfexchange.controller.mip;

import static uz.fido.pfexchange.config.Authority.Codes.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.mip.push.MipPushDeliveryStatisticsDailyReponseDto;
import uz.fido.pfexchange.dto.mip.push.MipPushDetailedReportResponseDto;
import uz.fido.pfexchange.dto.mip.push.MipPushPensionRequestDataDto;
import uz.fido.pfexchange.service.mip.MipPushService;
import uz.fido.pfexchange.utils.ResponseBuilder;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pf/mip/push")
@Tag(name = "MIP Push", description = "MIP Push xizmati bilan ma'lumotlarni almashish API'lari")
public class MipPushController {

    private final MipPushService mipPushService;

    @GetMapping("/token")
    @PreAuthorize("hasAuthority('" + INTERNAL_MIP_PUSH_TOKEN + "')")
    @Operation(summary = "MIP Push token", description = "MIP Push xizmati uchun token olish")
    public ResponseEntity<ResponseWrapperDto<String>> token() {
        return ResponseBuilder.ok(mipPushService.token());
    }

    @GetMapping("/delivery-detailed-report/{uuid}")
    @PreAuthorize("hasAuthority('" + INTERNAL_MIP_PUSH_DETAILED_REPORT + "')")
    @Operation(
            summary = "MIP Push so'rov hisoboti",
            description = "MIP Push xizmatidan so'rov hisobotini olish")
    public ResponseEntity<ResponseWrapperDto<MipPushDetailedReportResponseDto>>
            deliveryDetailedReport(@PathVariable String uuid) {
        return ResponseBuilder.ok(mipPushService.deliveryDetailedReport(uuid));
    }

    @GetMapping("/delivery-statistics")
    @PreAuthorize("hasAuthority('" + INTERNAL_MIP_PUSH_DELIVERY_STATISTICS_DAILY + "')")
    @Operation(
            summary = "MIP Push kunlik statistika",
            description = "MIP Push xizmatidan kunlik statistikalarini olish")
    public ResponseEntity<ResponseWrapperDto<List<MipPushDeliveryStatisticsDailyReponseDto>>>
            deliveryStatisticsDaily() {
        return ResponseBuilder.ok(mipPushService.deliveryStatisticsDaily());
    }

    @GetMapping("/ping")
    @PreAuthorize("hasAuthority('" + INTERNAL_MIP_PUSH_PING + "')")
    @Operation(summary = "MIP Push ping", description = "MIP Push xizmatidan pingni olish")
    public ResponseEntity<ResponseWrapperDto<String>> ping() {
        return ResponseBuilder.ok(mipPushService.ping());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + INTERNAL_MIP_PUSH_PUSH + "')")
    @Operation(summary = "MIP Push", description = "MIP Push xizmatiga ma'lumot yuborish")
    public ResponseEntity<ResponseWrapperDto<String>> push(
            @Valid @RequestBody MipPushPensionRequestDataDto requestDto) {
        return ResponseBuilder.ok(mipPushService.push(requestDto));
    }

    @PostMapping("/async")
    @PreAuthorize("hasAuthority('" + INTERNAL_MIP_PUSH_PUSH + "')")
    @Operation(summary = "MIP Push", description = "MIP Push xizmatiga ma'lumot yuborish")
    public ResponseEntity<ResponseWrapperDto<String>> pushAsync(
            @Valid @RequestBody MipPushPensionRequestDataDto requestDto) {
        return ResponseBuilder.ok(mipPushService.pushAsync(requestDto));
    }
}
