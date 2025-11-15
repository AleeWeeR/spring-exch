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
     *
     * @param pinfl Shaxsiy identifikatsiya raqami (14 xonali)
     * @param wsId Veb-servis identifikatori
     * @return Oracle funksiyasining natijasi (return code va JSON javob)
     */
    public MipFunctionResultDto callCheckPensionerStatus(String pinfl, Long wsId) {
        // Oracle paket va funksiya nomlari
        final String CATALOG_NAME = "PF_EXCHANGES_WS_ID";
        final String FUNCTION_NAME = "CHECK_PENSIONER_STATUS";
        final String RETURN_KEY = "RETURN";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    // 1. Function Return Value
                    new SqlOutParameter(RETURN_KEY, Types.INTEGER),
                    // 2. p_Pinfl (IN)
                    new SqlParameter("p_Pinfl", Types.VARCHAR),
                    // 3. p_Ws_Id (IN)
                    new SqlParameter("p_Ws_Id", Types.BIGINT),
                    // 4. o_Out_Text (OUT) - JSON javob
                    new SqlOutParameter("o_Out_Text", Types.CLOB)
                );

            Map<String, Object> result = jdbcCall.execute(
                Map.of("p_Pinfl", pinfl, "p_Ws_Id", wsId)
            );

            // Return kodini olish
            Integer returnValue = (Integer) result.get(RETURN_KEY);

            // CLOB dan JSON matnni olish
            Clob clobData = (Clob) result.get("o_Out_Text");
            String jsonText = clobToString(clobData);

            log.info(
                "Function CHECK_PENSIONER_STATUS executed. Return code: {}, JSON length: {}",
                returnValue,
                jsonText != null ? jsonText.length() : 0
            );

            return MipFunctionResultDto.builder()
                .returnCode(returnValue)
                .jsonText(jsonText)
                .build();
        } catch (Exception e) {
            log.error(
                "Error calling CHECK_PENSIONER_STATUS function for PINFL: {}, WS_ID: {}",
                pinfl,
                wsId,
                e
            );
            throw new RuntimeException(
                "Failed to call CHECK_PENSIONER_STATUS Oracle function",
                e
            );
        }
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
