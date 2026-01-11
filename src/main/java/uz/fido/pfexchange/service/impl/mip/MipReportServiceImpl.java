package uz.fido.pfexchange.service.impl.mip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import uz.fido.pfexchange.dto.mip.MipFunctionResultDto;
import uz.fido.pfexchange.dto.mip.report.MipReportRequestDto;
import uz.fido.pfexchange.dto.mip.report.MipReportResponseDto;
import uz.fido.pfexchange.repository.mip.MipReportRepository;
import uz.fido.pfexchange.service.mip.MipReportService;
import uz.fido.pfexchange.utils.JsonUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MipReportServiceImpl implements MipReportService {

    private final MipReportRepository mipOracleRepository;

    @Override
    public MipReportResponseDto pensionInfo(MipReportRequestDto requestDto) {
        MipFunctionResultDto functionResult =
                mipOracleRepository.callSendInfo(requestDto.getPinfl(), requestDto.getWsId());
        if (functionResult.getReturnCode() == null || functionResult.getReturnCode() != 200) {
            log.error("Oracle function returned error code: {}", functionResult.getReturnCode());
            throw new RuntimeException(
                    "Oracle function failed with code: " + functionResult.getReturnCode());
        }

        try {
            if (functionResult.getJsonText() == null || functionResult.getJsonText().isEmpty()) {
                log.warn("Empty JSON response from Oracle function");
                return MipReportResponseDto.builder().build();
            }

            return JsonUtils.fromSnakeCaseJson(
                    functionResult.getJsonText(), MipReportResponseDto.class);
        } catch (Exception e) {
            log.error("Error parsing JSON response from Oracle", e);
            throw new RuntimeException("Failed to parse Oracle response", e);
        }
    }
}
