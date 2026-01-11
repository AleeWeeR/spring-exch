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
        final String CATALOG_NAME = "PF_EXCHANGES_MIP_REPORT";
        final String FUNCTION_NAME = "SEND_INFO";
        final String RETURN_KEY = "RETURN";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter(RETURN_KEY, Types.INTEGER),
                    new SqlParameter("p_Pinpp", Types.VARCHAR),
                    new SqlParameter("p_Ws_Id", Types.BIGINT),
                    new SqlOutParameter("o_Out_Text", Types.CLOB)
                );

            Map<String, Object> result = jdbcCall.execute(
                Map.of("p_Pinpp", pinpp, "p_Ws_Id", wsId)
            );

            Integer returnValue = (Integer) result.get(RETURN_KEY);

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
                "Error calling Send_Info function for WS_ID: {}",
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
            return clob.getSubString(1, (int) length);
        } catch (Exception e) {
            log.error("Error converting CLOB to String", e);
            throw new RuntimeException("Failed to read CLOB data", e);
        }
    }
}
