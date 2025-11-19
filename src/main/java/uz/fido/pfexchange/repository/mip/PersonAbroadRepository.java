package uz.fido.pfexchange.repository.mip;

import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

/**
 * Repository for person abroad status Oracle function calls
 * Calls PF_EXCHANGES_ABROAD package functions
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PersonAbroadRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Check person status (read-only, no restoration)
     * Calls PF_EXCHANGES_ABROAD.Check_Person_Status
     *
     * @param xmlData Request data in XML format (from JSON)
     * @return Map with RETURN (0/1) and O_DATA (JSON CLOB)
     */
    public Map<String, Object> checkPersonStatus(String xmlData) {
        final String CATALOG_NAME = "PF_EXCHANGES_ABROAD";
        final String FUNCTION_NAME = "Check_Person_Status";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("RETURN", Types.INTEGER),
                    new SqlOutParameter("O_Data", Types.CLOB),
                    new SqlParameter("P_Data", Types.VARCHAR)
                );

            Map<String, Object> result = jdbcCall.execute(Map.of("P_Data", xmlData));

            log.debug("Check_Person_Status called, Return code: {}", result.get("RETURN"));
            return result;
        } catch (Exception e) {
            log.error("Error calling Check_Person_Status", e);
            throw new RuntimeException("Failed to check person status", e);
        }
    }

    /**
     * Check arrival and restore person if needed
     * Calls PF_EXCHANGES_ABROAD.Restore_Person_Status
     *
     * @param xmlData Request data in XML format (from JSON)
     * @return Map with RETURN (0/1) and O_DATA (JSON CLOB)
     */
    public Map<String, Object> restorePersonStatus(String xmlData) {
        final String CATALOG_NAME = "PF_EXCHANGES_ABROAD";
        final String FUNCTION_NAME = "Restore_Person_Status";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("RETURN", Types.INTEGER),
                    new SqlOutParameter("O_Data", Types.CLOB),
                    new SqlParameter("P_Data", Types.VARCHAR)
                );

            Map<String, Object> result = jdbcCall.execute(Map.of("P_Data", xmlData));

            log.debug("Restore_Person_Status called, Return code: {}", result.get("RETURN"));
            return result;
        } catch (Exception e) {
            log.error("Error calling Restore_Person_Status", e);
            throw new RuntimeException("Failed to restore person status", e);
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
