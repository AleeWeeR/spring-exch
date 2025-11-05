package uz.fido.pfexchange.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
        MipReportResponseDto response = mipReportService.pensionInfo(
            requestDto
        );

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseBuilder.ok(mipReportService.pensionInfo(requestDto));
    }
}
