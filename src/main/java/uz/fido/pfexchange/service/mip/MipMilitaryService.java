package uz.fido.pfexchange.service.mip;

import uz.fido.pfexchange.dto.military.MilitaryRequestDto;
import uz.fido.pfexchange.dto.military.MilitaryResponseDto;

public interface MipMilitaryService {
    MilitaryResponseDto sendRequest(MilitaryRequestDto requestDto);  
}
