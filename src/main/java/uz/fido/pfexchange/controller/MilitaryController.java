package uz.fido.pfexchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.military.MilitaryRequestDto;
import uz.fido.pfexchange.dto.military.MilitaryResponseDto;
import uz.fido.pfexchange.service.MilitaryService;
import uz.fido.pfexchange.utils.ResponseBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("pf/pf/military")
public class MilitaryController {

    private final MilitaryService militaryService;

    @PostMapping
    public ResponseEntity<ResponseWrapperDto<MilitaryResponseDto>> sendRequest(
        @RequestBody MilitaryRequestDto requestDto
    ) {
        return ResponseBuilder.ok(
            militaryService.sendRequest(requestDto)
        );
    }
}
