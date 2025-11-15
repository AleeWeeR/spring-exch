package uz.fido.pfexchange.repository.mip;

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
 * Uses separate repository-layer Oracle functions for better separation of concerns
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PersonAbroadRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Check if person is active in the system
     *
     * @param pinfl Person's PINFL
     * @return -1=not found, 0=inactive, 1=active
     */
    public Integer isPersonActive(String pinfl) {
        final String CATALOG_NAME = "Pf_Person_Repository";
        final String FUNCTION_NAME = "Is_Person_Active";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("RETURN", Types.INTEGER),
                    new SqlParameter("p_Pinfl", Types.VARCHAR)
                );

            Map<String, Object> result = jdbcCall.execute(Map.of("p_Pinfl", pinfl));
            Integer returnValue = (Integer) result.get("RETURN");

            log.debug("Is_Person_Active called for PINFL: {}, Result: {}", pinfl, returnValue);
            return returnValue;
        } catch (Exception e) {
            log.error("Error calling Is_Person_Active for PINFL: {}", pinfl, e);
            throw new RuntimeException("Failed to check person active status", e);
        }
    }

    /**
     * Get person's closure status
     *
     * @param pinfl Person's PINFL
     * @return Map with close_reason, close_date, close_desc
     */
    public Map<String, Object> getPersonCloseStatus(String pinfl) {
        final String CATALOG_NAME = "Pf_Person_Repository";
        final String FUNCTION_NAME = "Get_Person_Close_Status";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("RETURN", Types.INTEGER),
                    new SqlParameter("p_Pinfl", Types.VARCHAR),
                    new SqlOutParameter("o_Close_Reason", Types.VARCHAR),
                    new SqlOutParameter("o_Close_Date", Types.DATE),
                    new SqlOutParameter("o_Close_Desc", Types.VARCHAR)
                );

            Map<String, Object> result = jdbcCall.execute(Map.of("p_Pinfl", pinfl));

            log.debug("Get_Person_Close_Status called for PINFL: {}", pinfl);
            return result;
        } catch (Exception e) {
            log.error("Error calling Get_Person_Close_Status for PINFL: {}", pinfl, e);
            throw new RuntimeException("Failed to get person close status", e);
        }
    }

    /**
     * Check if citizen has arrived back to Uzbekistan
     *
     * @param personId Person ID
     * @param pinfl Person's PINFL
     * @param birthDate Person's birth date
     * @return 1=arrived, 0=not arrived
     */
    public Map<String, Object> checkCitizenArrival(Long personId, String pinfl, java.sql.Date birthDate) {
        final String CATALOG_NAME = "Pf_Person_Abroad_Repository";
        final String FUNCTION_NAME = "Check_Citizen_Arrival";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("RETURN", Types.INTEGER),
                    new SqlParameter("p_Person_Id", Types.BIGINT),
                    new SqlParameter("p_Pinfl", Types.VARCHAR),
                    new SqlParameter("p_Birth_Date", Types.DATE),
                    new SqlOutParameter("o_Message", Types.VARCHAR)
                );

            Map<String, Object> result = jdbcCall.execute(
                Map.of(
                    "p_Person_Id", personId,
                    "p_Pinfl", pinfl,
                    "p_Birth_Date", birthDate
                )
            );

            log.info("Check_Citizen_Arrival called for Person ID: {}, Result: {}",
                personId, result.get("RETURN"));
            return result;
        } catch (Exception e) {
            log.error("Error calling Check_Citizen_Arrival for Person ID: {}", personId, e);
            throw new RuntimeException("Failed to check citizen arrival", e);
        }
    }

    /**
     * Restore person who has arrived back
     *
     * @param personId Person ID
     * @return 1=success, 0=failed
     */
    public Map<String, Object> restoreArrivedPerson(Long personId) {
        final String CATALOG_NAME = "Pf_Person_Abroad_Repository";
        final String FUNCTION_NAME = "Restore_Arrived_Person";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withFunctionName(FUNCTION_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("RETURN", Types.INTEGER),
                    new SqlParameter("p_Person_Id", Types.BIGINT),
                    new SqlOutParameter("o_Message", Types.VARCHAR)
                );

            Map<String, Object> result = jdbcCall.execute(
                Map.of("p_Person_Id", personId)
            );

            log.info("Restore_Arrived_Person called for Person ID: {}, Result: {}",
                personId, result.get("RETURN"));
            return result;
        } catch (Exception e) {
            log.error("Error calling Restore_Arrived_Person for Person ID: {}", personId, e);
            throw new RuntimeException("Failed to restore arrived person", e);
        }
    }

    /**
     * Log the status check request
     *
     * @param wsId Web service ID
     * @param pinfl Person's PINFL
     * @param inputData Original request data
     * @param resultCode Result code
     * @param message Result message
     * @param status Person status (optional)
     */
    public void logStatusRequest(Long wsId, String pinfl, String inputData,
                                  Integer resultCode, String message, Integer status) {
        final String CATALOG_NAME = "Pf_Person_Abroad_Repository";
        final String PROCEDURE_NAME = "Log_Status_Request";

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(CATALOG_NAME)
                .withProcedureName(PROCEDURE_NAME)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlParameter("p_Ws_Id", Types.BIGINT),
                    new SqlParameter("p_Pinfl", Types.VARCHAR),
                    new SqlParameter("p_In_Data", Types.CLOB),
                    new SqlParameter("p_Result_Code", Types.INTEGER),
                    new SqlParameter("p_Msg", Types.VARCHAR),
                    new SqlParameter("p_Status", Types.INTEGER)
                );

            jdbcCall.execute(
                Map.of(
                    "p_Ws_Id", wsId,
                    "p_Pinfl", pinfl,
                    "p_In_Data", inputData != null ? inputData : "",
                    "p_Result_Code", resultCode,
                    "p_Msg", message != null ? message : "",
                    "p_Status", status != null ? status : 0
                )
            );

            log.debug("Request logged for PINFL: {}, Result Code: {}", pinfl, resultCode);
        } catch (Exception e) {
            // Don't fail the main operation if logging fails
            log.warn("Failed to log status request for PINFL: {}", pinfl, e);
        }
    }

    /**
     * Get person ID by PINFL
     * This is a simple query, so we can use JdbcTemplate directly
     */
    public Long getPersonIdByPinfl(String pinfl) {
        try {
            String sql = "SELECT Person_Id FROM Pf_Persons WHERE Pinpp = ? AND Person_Type = '01' AND ROWNUM = 1";
            return jdbcTemplate.queryForObject(sql, Long.class, pinfl);
        } catch (Exception e) {
            log.debug("Person not found for PINFL: {}", pinfl);
            return null;
        }
    }

    /**
     * Get person birth date by person ID
     */
    public java.sql.Date getPersonBirthDate(Long personId) {
        try {
            String sql = "SELECT Birth_Date FROM Pf_Persons WHERE Person_Id = ?";
            return jdbcTemplate.queryForObject(sql, java.sql.Date.class, personId);
        } catch (Exception e) {
            log.error("Failed to get birth date for Person ID: {}", personId, e);
            return null;
        }
    }
}
