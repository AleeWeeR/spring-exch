package uz.fido.pfexchange.repository.mib;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * Repository for debt cancellation Oracle function calls
 * Calls PF_EXCHANGES_MIB package functions
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DebtCancellationRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Calls Task_Data_Send_Cancel_Debt Oracle function
     * This is the legacy batch function - kept for compatibility
     *
     * @param externalId External ID (inventory_id)
     * @return Map with RETURN (0/1) and O_OUT_TEXT (result message)
     */
    public Map<String, Object> sendCancelDebt(Long externalId) {
        final String CATALOG_NAME = "PF_EXCHANGES_MIB";
        final String FUNCTION_NAME = "Task_Data_Send_Cancel_Debt";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("RETURN", Types.INTEGER),
                    new SqlOutParameter("o_Out_Text", Types.VARCHAR),
                    new SqlParameter("p_external_id", Types.NUMERIC)
                );

            Map<String, Object> result = jdbcCall.execute(Map.of("p_external_id", externalId));

            log.debug("Task_Data_Send_Cancel_Debt called for external_id: {}, Return code: {}",
                externalId, result.get("RETURN"));
            return result;
        } catch (Exception e) {
            log.error("Error calling Task_Data_Send_Cancel_Debt for external_id: {}", externalId, e);
            throw new RuntimeException("Failed to send cancel debt request", e);
        }
    }

    /**
     * Calls Request_Cancel_Debt Oracle function
     * This updates the database after receiving MIB response
     *
     * @param outText Result message from MIB
     * @param reportXml XML report data
     * @return Result code (1 = success, 0 = failure)
     */
    public Integer requestCancelDebt(String outText, String reportXml) {
        final String CATALOG_NAME = "PF_EXCHANGES_MIB";
        final String FUNCTION_NAME = "Request_Cancel_Debt";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("RETURN", Types.INTEGER),
                    new SqlParameter("v_Out_Text", Types.VARCHAR),
                    new SqlParameter("v_Report", Types.CLOB)
                );

            Map<String, Object> result = jdbcCall.execute(Map.of(
                "v_Out_Text", outText,
                "v_Report", reportXml
            ));

            Integer returnCode = (Integer) result.get("RETURN");
            log.debug("Request_Cancel_Debt called, Return code: {}", returnCode);
            return returnCode;
        } catch (Exception e) {
            log.error("Error calling Request_Cancel_Debt", e);
            throw new RuntimeException("Failed to process cancel debt response", e);
        }
    }

    /**
     * Convert CLOB to String
     */
    public String clobToString(Clob clob) {
        if (clob == null) {
            return null;
        }
        try {
            long length = clob.length();
            return clob.getSubString(1, (int) length);
        } catch (SQLException e) {
            log.error("Error converting CLOB to String", e);
            throw new RuntimeException("Failed to convert CLOB to String", e);
        }
    }
}
