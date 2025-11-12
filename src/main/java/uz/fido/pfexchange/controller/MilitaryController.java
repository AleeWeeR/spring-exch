package uz.fido.pfexchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.military.MilitaryRequestDto;
import uz.fido.pfexchange.dto.military.MilitaryResponseDto;
import uz.fido.pfexchange.service.MilitaryService;
import uz.fido.pfexchange.utils.ResponseBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/pf/military")
public class MilitaryController {

    private final MilitaryService militaryService;

    @PostMapping(
        consumes = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
        }
    )
    public ResponseEntity<ResponseWrapperDto<MilitaryResponseDto>> sendRequest(
        @Valid @RequestBody MilitaryRequestDto requestDto
    ) {
        return ResponseBuilder.ok(militaryService.sendRequest(requestDto));
    }
}
