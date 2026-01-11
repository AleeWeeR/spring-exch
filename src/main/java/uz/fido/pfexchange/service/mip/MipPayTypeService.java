package uz.fido.pfexchange.service.mip;

import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeChangeRequestDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeChangeResponseDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeRequestDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeResponseDto;

public interface MipPayTypeService {
    MipPayTypeResponseDto payTypeInfo(MipPayTypeRequestDto requestDto);

    MipPayTypeChangeResponseDto payTypeChange(MipPayTypeChangeRequestDto requestDto);
}
