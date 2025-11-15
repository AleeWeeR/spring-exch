package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.dto.mip.MipFunctionResultDto;
import uz.fido.pfexchange.dto.mip.WsIdStatusRequestDto;
import uz.fido.pfexchange.dto.mip.WsIdStatusResponseDto;
import uz.fido.pfexchange.repository.mip.WsIdStatusRepository;
import uz.fido.pfexchange.service.WsIdStatusService;

/**
 * Pensiya oluvchilar holati uchun servis implementatsiyasi
 * Service implementation for pension recipient status
 *
 * Natija kodlari:
 * 0 - Pensiya oluvchilar ro'yhatida mavjud emas
 * 1 - Pensiya oluvchilar ro'yhatida mavjud
 * 2 - Oluvchi statusi faol xolatga keltirildi
 * 3 - O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WsIdStatusServiceImpl implements WsIdStatusService {

    private final WsIdStatusRepository wsIdStatusRepository;
    private final ObjectMapper objectMapper;

    @Override
    public WsIdStatusResponseDto checkStatus(WsIdStatusRequestDto requestDto) {
        String pinfl = requestDto.getData().getPinfl();
        Long wsId = requestDto.getData().getWsId();

        log.info(
            "Checking pension recipient status for PINFL: {}, WS_ID: {}",
            pinfl,
            wsId
        );

        // Oracle funksiyasini chaqirish
        MipFunctionResultDto functionResult = wsIdStatusRepository
            .callCheckPensionerStatus(pinfl, wsId);

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

        // JSON javobni parsing qilish
        try {
            if (
                functionResult.getJsonText() == null ||
                functionResult.getJsonText().isEmpty()
            ) {
                log.warn("Empty JSON response from Oracle function");
                return WsIdStatusResponseDto.builder()
                    .result(0)
                    .msg("Ma'lumot topilmadi")
                    .wsId(wsId)
                    .build();
            }

            // JSON ni WsIdStatusResponseDto ga o'tkazish
            WsIdStatusResponseDto response = objectMapper.readValue(
                functionResult.getJsonText(),
                WsIdStatusResponseDto.class
            );

            log.info(
                "Successfully checked status for PINFL: {}, Result: {}, Message: {}",
                pinfl,
                response.getResult(),
                response.getMsg()
            );

            return response;
        } catch (Exception e) {
            log.error("Error parsing JSON response from Oracle", e);
            throw new RuntimeException("Failed to parse Oracle response", e);
        }
    }
}
