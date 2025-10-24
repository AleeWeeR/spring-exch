package uz.fido.pfexchange.service;

import uz.fido.pfexchange.dto.military.MilitaryRequestDto;
import uz.fido.pfexchange.dto.military.MilitaryResponseDto;

public interface MilitaryService {
    MilitaryResponseDto sendRequest(MilitaryRequestDto requestDto, String username, String password, String url);  
}
