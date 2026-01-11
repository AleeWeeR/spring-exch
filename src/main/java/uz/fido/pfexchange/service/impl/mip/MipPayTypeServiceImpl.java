package uz.fido.pfexchange.service.impl.mip;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import uz.fido.pfexchange.dto.mip.MipFunctionResultDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeChangeRequestDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeChangeResponseDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeRequestDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeResponseDto;
import uz.fido.pfexchange.repository.mip.MipPayTypeRepository;
import uz.fido.pfexchange.service.mip.MipPayTypeService;
import uz.fido.pfexchange.utils.JsonUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MipPayTypeServiceImpl implements MipPayTypeService {

    private final MipPayTypeRepository mipOracleRepository;

    @Override
    public MipPayTypeResponseDto payTypeInfo(MipPayTypeRequestDto requestDto) {
        MipFunctionResultDto functionResult = mipOracleRepository.callPayTypeInfo(requestDto);

        String json = functionResult.getJsonText();
        if (json == null || json.isEmpty()) {
            log.warn("Empty JSON response from Oracle function");
            return MipPayTypeResponseDto.builder().build();
        }

        return JsonUtils.fromSnakeCaseJson(json, MipPayTypeResponseDto.class);
    }

    @Override
    public MipPayTypeChangeResponseDto payTypeChange(MipPayTypeChangeRequestDto requestDto) {
        MipFunctionResultDto functionResult = mipOracleRepository.callPayTypeChange(requestDto);

        String json = functionResult.getJsonText();
        if (json == null || json.isEmpty()) {
            log.warn("Empty JSON response from Oracle function");
            return MipPayTypeChangeResponseDto.builder().build();
        }

        return JsonUtils.fromSnakeCaseJson(json, MipPayTypeChangeResponseDto.class);
    }
}
