package uz.fido.pfexchange.service.impl.mip;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.mip.charge.MipChargeHistoryResponseDto;
import uz.fido.pfexchange.dto.mip.charge.MipChargeRequestDto;
import uz.fido.pfexchange.dto.mip.charge.MipChargeResponseDto;
import uz.fido.pfexchange.exception.RestException;
import uz.fido.pfexchange.service.mip.MipChargeService;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Types;

/**
 * Implementation of ChargeService
 * Calls Oracle PL/SQL functions from PF_EXCHANGES_EP_CHARGE package
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MipChargeServiceImpl implements MipChargeService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public MipChargeResponseDto getChargesInfo(MipChargeRequestDto request) {
        log.info("Calling Get_Charges_Info for ws_id: {}, pinfl: {}", request.getWsId(), request.getPinfl());

        String xmlInput = buildXmlInput(request);

        try {
            return jdbcTemplate.execute((Connection connection) -> {
                String sql = "{? = call PF_EXCHANGES_EP_CHARGE.Get_Charges_Info(?, ?)}";

                try (CallableStatement cs = connection.prepareCall(sql)) {
                    // Register output parameters
                    cs.registerOutParameter(1, Types.NUMERIC);  // Return value
                    cs.registerOutParameter(2, Types.CLOB);    // O_Data (CLOB)

                    // Set input parameter
                    cs.setString(3, xmlInput);  // P_Data (VARCHAR2)

                    // Execute
                    cs.execute();

                    // Get results
                    int returnValue = cs.getInt(1);
                    Clob clobData = cs.getClob(2);

                    String jsonResponse = clobToString(clobData);
                    log.debug("Get_Charges_Info returned: {}, JSON: {}", returnValue, jsonResponse);

                    // Parse JSON response to DTO
                    return objectMapper.readValue(jsonResponse, MipChargeResponseDto.class);

                } catch (Exception e) {
                    log.error("Error calling Get_Charges_Info", e);
                    throw  RestException.restThrow(
                            ResponseWrapperDto.builder()
                                    .code(Constants.ERROR)
                                    .message("Kutilmagan xatolik: " + e.getMessage())
                                    .build(),
                            HttpStatus.INTERNAL_SERVER_ERROR);

                }
            });


        } catch (Exception e) {
            log.error("Database error in getChargesInfo", e);
            throw RestException.restThrow(
                    ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message("Kutilmagan xatolik: " + e.getMessage())
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public MipChargeHistoryResponseDto getChargedInfo(MipChargeRequestDto request) {
        log.info("Calling Get_Charged_Info for ws_id: {}, pinfl: {}", request.getWsId(), request.getPinfl());

        String xmlInput = buildXmlInput(request);

        try {
            return jdbcTemplate.execute((Connection connection) -> {
                String sql = "{? = call PF_EXCHANGES_EP_CHARGE.Get_Charged_Info(?, ?)}";

                try (CallableStatement cs = connection.prepareCall(sql)) {
                    // Register output parameters
                    cs.registerOutParameter(1, Types.NUMERIC);  // Return value
                    cs.registerOutParameter(2, Types.CLOB);    // O_Data (CLOB)

                    // Set input parameter
                    cs.setString(3, xmlInput);  // P_Data (VARCHAR2)

                    // Execute
                    cs.execute();

                    // Get results
                    int returnValue = cs.getInt(1);
                    Clob clobData = cs.getClob(2);

                    String jsonResponse = clobToString(clobData);
                    log.debug("Get_Charged_Info returned: {}, JSON: {}", returnValue, jsonResponse);

                    // Parse JSON response to DTO
                    return objectMapper.readValue(jsonResponse, MipChargeHistoryResponseDto.class);

                } catch (Exception e) {
                    log.error("Error calling Get_Charged_Info", e);
                    throw RestException.restThrow(
                            ResponseWrapperDto.builder()
                                    .code(Constants.ERROR)
                                    .message("Kutilmagan xatolik: " + e.getMessage())
                                    .build(),
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });

        } catch (Exception e) {
            log.error("Database error in getChargedInfo", e);
            throw  RestException.restThrow(
                    ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message("Kutilmagan xatolik: " + e.getMessage())
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Build XML input string for Oracle function
     * Format: <Data><ws_id>123</ws_id><pinfl>12345678901234</pinfl></Data>
     */
    private String buildXmlInput(MipChargeRequestDto request) {
        return "<Data>" +
                "<ws_id>" + request.getWsId() + "</ws_id>" +
                "<pinfl>" + request.getPinfl() + "</pinfl>" +
                "</Data>";
    }

    /**
     * Convert CLOB to String
     */
    private String clobToString(Clob clob) throws Exception {
        if (clob == null) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        try (java.io.Reader reader = clob.getCharacterStream()) {
            char[] buffer = new char[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
        }

        return sb.toString();
    }


}
