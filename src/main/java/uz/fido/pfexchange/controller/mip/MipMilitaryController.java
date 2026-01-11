package uz.fido.pfexchange.controller.mip;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.military.MilitaryRequestDto;
import uz.fido.pfexchange.dto.military.MilitaryResponseDto;
import uz.fido.pfexchange.service.mip.MipMilitaryService;
import uz.fido.pfexchange.utils.ResponseBuilder;
import static uz.fido.pfexchange.config.Authority.Codes.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/pf/mip/military")
@Tag(name = "Harbiy ish tajbirasi", description = "Harbiy ish tajbirasi haqida ma'lumotlar")
public class MipMilitaryController {

    private final MipMilitaryService militaryService;

    @PostMapping(
            consumes = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE,
            },
            produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE,
            })
    @PreAuthorize("hasAuthority('" + INTERNAL_MILITARY_SEND_REQUEST + "')")
    @Operation(
            summary = "Harbiy ish tajbirasi so'rov yuborish",
            description = "Harbiy ish tajbirasi so'rov yuborish uchun foydalanuvchi ma'lumotlarini yuborish"
    )
    public ResponseEntity<ResponseWrapperDto<MilitaryResponseDto>> sendRequest(@Valid @RequestBody MilitaryRequestDto requestDto) {

        MilitaryResponseDto responseDto = militaryService.sendRequest(requestDto);

        return ResponseBuilder.ok(responseDto);
    }
}
