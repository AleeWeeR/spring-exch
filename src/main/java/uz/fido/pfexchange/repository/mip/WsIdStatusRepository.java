package uz.fido.pfexchange.repository.mip;

import java.sql.Clob;
import java.sql.Types;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import uz.fido.pfexchange.dto.mip.MipFunctionResultDto;

/**
 * Repository for WS ID status Oracle function calls
 * Pensiya oluvchilar holati uchun Oracle funksiyalarini chaqirish
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class WsIdStatusRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Oracle funksiyasi: Pensiya oluvchining holatini tekshirish va faollashtirish
     * Uses existing Pf_Person_Abroad.Citizen_Arrived and Restore_Person_Arrived functions
     *
     * @param pinfl Shaxsiy identifikatsiya raqami (14 xonali)
     * @param wsId Veb-servis identifikatori
     * @return Oracle funksiyasining natijasi (return code va JSON javob)
     */
    public MipFunctionResultDto callCheckPensionerStatus(String pinfl, Long wsId) {
        // Oracle paket va funksiya nomlari
        final String CATALOG_NAME = "PF_EXCHANGES_WS_ID";
        final String FUNCTION_NAME = "CHECK_PERSON_STATUS";
        final String RETURN_KEY = "RETURN";

        try {
            // Build XML input like the charge controller
            String xmlInput = buildXmlInput(pinfl, wsId);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    // 1. Function Return Value
                    new SqlOutParameter(RETURN_KEY, Types.INTEGER),
                    // 2. O_Data (OUT) - JSON response
                    new SqlOutParameter("O_Data", Types.CLOB),
                    // 3. P_Data (IN) - XML input
                    new SqlParameter("P_Data", Types.VARCHAR)
                );

            Map<String, Object> result = jdbcCall.execute(
                Map.of("P_Data", xmlInput)
            );

            // Return kodini olish
            Integer returnValue = (Integer) result.get(RETURN_KEY);

            // CLOB dan JSON matnni olish
            Clob clobData = (Clob) result.get("O_Data");
            String jsonText = clobToString(clobData);

            log.info(
                "Function CHECK_PERSON_STATUS executed. Return code: {}, JSON length: {}",
                returnValue,
                jsonText != null ? jsonText.length() : 0
            );

            return MipFunctionResultDto.builder()
                .returnCode(returnValue)
                .jsonText(jsonText)
                .build();
        } catch (Exception e) {
            log.error(
                "Error calling CHECK_PERSON_STATUS function for PINFL: {}, WS_ID: {}",
                pinfl,
                wsId,
                e
            );
            throw new RuntimeException(
                "Failed to call CHECK_PERSON_STATUS Oracle function",
                e
            );
        }
    }

    /**
     * Build XML input matching the pattern from charge controller
     */
    private String buildXmlInput(String pinfl, Long wsId) {
        return "<Data>" +
               "<ws_id>" + wsId + "</ws_id>" +
               "<pinfl>" + pinfl + "</pinfl>" +
               "</Data>";
    }

    /**
     * CLOB ni String ga o'zgartirish
     */
    private String clobToString(Clob clob) {
        if (clob == null) {
            return null;
        }

        try {
            long length = clob.length();
            if (length == 0) {
                return "";
            }
            return clob.getSubString(1, (int) length);
        } catch (Exception e) {
            log.error("Error converting CLOB to String", e);
            throw new RuntimeException("Failed to read CLOB data", e);
        }
    }
}
