package uz.fido.pfexchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.mip.MipReportRequestDto;
import uz.fido.pfexchange.dto.mip.MipReportResponseDto;
import uz.fido.pfexchange.service.MipReportService;
import uz.fido.pfexchange.utils.ResponseBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/pf/mip/report")
public class MipReportController {

    private final MipReportService mipReportService;

    @PreAuthorize(value = "hasAnyAuthority('GET_MIP_INFO')")
    @PostMapping(
        value = "/pension-info",
        consumes = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
        }
    )
    public ResponseEntity<ResponseWrapperDto<MipReportResponseDto>> pensionInfo(
        @Valid @RequestBody MipReportRequestDto requestDto
    ) {
        return ResponseBuilder.ok(mipReportService.pensionInfo(requestDto));
    }
}
