package uz.fido.pfexchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.military.MilitaryRequestDto;
import uz.fido.pfexchange.dto.military.MilitaryResponseDto;
import uz.fido.pfexchange.dto.minyust.MinyustFamilyBatchResponseDto;
import uz.fido.pfexchange.service.MinyustFamilyBatchRequestProcessor;
import uz.fido.pfexchange.utils.MinyustFamilyBatchStatus;
import uz.fido.pfexchange.utils.ResponseBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("pf/pf/military")
public class MilitaryController {
    
    private final MilitaryService militaryService;

    @PostMapping( "send-by-pnfl")
    public ResponseEntity<ResponseWrapperDto<MilitaryResponseDto>> sendRequest(
        @RequestBody MilitaryRequestDto requestDto,
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String password,
        @RequestParam(required = false) String url
    ) {
        return militaryService.sendRequest(requestDto, username, password, url);
    }

}
