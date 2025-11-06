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

@Slf4j
@Repository
@RequiredArgsConstructor
public class MipReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public MipFunctionResultDto callSendInfo(String pinpp, Long wsId) {
        // Explicitly defining the constants for clarity
        final String CATALOG_NAME = "PF_EXCHANGES_MIP_REPORT";
        final String FUNCTION_NAME = "SEND_INFO";
        final String RETURN_KEY = "RETURN"; // Key used to retrieve function's return value

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess() // Prevents the ORA-00904 issue
                .declareParameters(
                    // 1. Function Return Value: Must be the first SqlOutParameter
                    new SqlOutParameter(RETURN_KEY, Types.INTEGER),
                    // 2. p_Pinpp (IN): Must match the first argument in the Oracle function definition
                    new SqlParameter("p_Pinpp", Types.VARCHAR),
                    // 3. p_Ws_Id (IN): Must match the second argument
                    new SqlParameter("p_Ws_Id", Types.BIGINT),
                    // 4. o_Out_Text (OUT): Must match the third argument
                    new SqlOutParameter("o_Out_Text", Types.CLOB)
                );

            Map<String, Object> result = jdbcCall.execute(
                Map.of("p_Pinpp", pinpp, "p_Ws_Id", wsId)
            );

            // Extract return value (function result)
            Integer returnValue = (Integer) result.get(RETURN_KEY);

            // Extract OUT parameter (CLOB)
            Clob clobData = (Clob) result.get("o_Out_Text");
            String jsonText = clobToString(clobData);

            log.info(
                "Function Send_Info executed. Return code: {}, JSON length: {}",
                returnValue,
                jsonText != null ? jsonText.length() : 0
            );

            return MipFunctionResultDto.builder()
                .returnCode(returnValue)
                .jsonText(jsonText)
                .build();
        } catch (Exception e) {
            log.error(
                "Error calling Send_Info function for PINPP: {}, WS_ID: {}",
                pinpp,
                wsId,
                e
            );
            throw new RuntimeException(
                "Failed to call Send_Info Oracle function",
                e
            );
        }
    }

    private String clobToString(Clob clob) {
        if (clob == null) {
            return null;
        }

        try {
            long length = clob.length();
            if (length == 0) {
                return "";
            }
            // Read entire CLOB content (for large CLOBs, consider streaming)
            return clob.getSubString(1, (int) length);
        } catch (Exception e) {
            log.error("Error converting CLOB to String", e);
            throw new RuntimeException("Failed to read CLOB data", e);
        }
    }
}
