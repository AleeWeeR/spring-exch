package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.dto.mip.MipFunctionResultDto;
import uz.fido.pfexchange.dto.mip.MipReportRequestDto;
import uz.fido.pfexchange.dto.mip.MipReportResponseDto;
import uz.fido.pfexchange.repository.mip.MipReportRepository;
import uz.fido.pfexchange.service.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MipReportServiceImpl implements MipReportService {

    private final MipReportRepository mipOracleRepository;
    private final ObjectMapper objectMapper;

    @Override
    public MipReportResponseDto pensionInfo(MipReportRequestDto requestDto) {
        log.info(
            "Requesting pension info for PINPP: {}",
            requestDto.getPinfl()
        );

        // Call Oracle function
        MipFunctionResultDto functionResult = mipOracleRepository.callSendInfo(
            requestDto.getPinfl(),
            requestDto.getWsId()
        );

        // Check return code
        if (
            functionResult.getReturnCode() == null ||
            functionResult.getReturnCode() != 200
        ) {
            log.error(
                "Oracle function returned error code: {}",
                functionResult.getReturnCode()
            );
            throw new RuntimeException(
                "Oracle function failed with code: " +
                    functionResult.getReturnCode()
            );
        }

        // Parse JSON from CLOB
        try {
            if (
                functionResult.getJsonText() == null ||
                functionResult.getJsonText().isEmpty()
            ) {
                log.warn("Empty JSON response from Oracle function");
                return MipReportResponseDto.builder().build();
            }

            // Parse JSON to your response DTO
            MipReportResponseDto response = objectMapper.readValue(
                functionResult.getJsonText(),
                MipReportResponseDto.class
            );

            log.info(
                "Successfully parsed pension info for PINPP: {}",
                requestDto.getPinfl()
            );
            return response;
        } catch (Exception e) {
            log.error("Error parsing JSON response from Oracle", e);
            throw new RuntimeException("Failed to parse Oracle response", e);
        }
    }
}
