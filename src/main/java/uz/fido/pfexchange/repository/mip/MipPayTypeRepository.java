package uz.fido.pfexchange.repository.mip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import uz.fido.pfexchange.dto.mip.MipFunctionResultDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeChangeRequestDto;
import uz.fido.pfexchange.dto.mip.paytype.MipPayTypeRequestDto;
import uz.fido.pfexchange.utils.XmlUtils;

import java.sql.Clob;
import java.sql.Types;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MipPayTypeRepository {

    private final JdbcTemplate jdbcTemplate;

    public MipFunctionResultDto callPayTypeInfo(MipPayTypeRequestDto requestDto) {
        final String CATALOG_NAME = "PF_EXCHANGES_MIP_PAY_TYPE";
        final String FUNCTION_NAME = "PAY_TYPE_INFO";
        final String RETURN_KEY = "RETURN";

        String xmlData = XmlUtils.toSnakeCaseXml(requestDto);
        log.info("XML data: {}", xmlData);

        try {
            SimpleJdbcCall jdbcCall =
                    new SimpleJdbcCall(jdbcTemplate)
                            .withCatalogName(CATALOG_NAME)
                            .withFunctionName(FUNCTION_NAME)
                            .withoutProcedureColumnMetaDataAccess()
                            .declareParameters(
                                    new SqlOutParameter(RETURN_KEY, Types.INTEGER),
                                    new SqlOutParameter("o_Data", Types.CLOB),
                                    new SqlParameter("p_Data", Types.VARCHAR));

            Map<String, Object> result = jdbcCall.execute(Map.of("p_Data", xmlData));

            Integer returnValue = (Integer) result.get(RETURN_KEY);

            Clob clobData = (Clob) result.get("o_Data");
            String jsonText = clobToString(clobData);

            log.info(
                    "Function Send_Info executed. Return code: {}, JSON length: {}",
                    returnValue,
                    jsonText != null ? jsonText.length() : 0);

            return MipFunctionResultDto.builder()
                    .returnCode(returnValue)
                    .jsonText(jsonText)
                    .build();
        } catch (Exception e) {
            log.error("Error calling Send_Info functio", e);
            throw new RuntimeException("Failed to call Send_Info Oracle function", e);
        }
    }

    public MipFunctionResultDto callPayTypeChange(MipPayTypeChangeRequestDto requestDto) {
        final String CATALOG_NAME = "PF_EXCHANGES_MIP_PAY_TYPE";
        final String FUNCTION_NAME = "PAY_TYPE_CHANGE";
        final String RETURN_KEY = "RETURN";

        String xmlData = XmlUtils.toSnakeCaseXml(requestDto);
        log.info("XML data: {}", xmlData);

        try {
            SimpleJdbcCall jdbcCall =
                    new SimpleJdbcCall(jdbcTemplate)
                            .withCatalogName(CATALOG_NAME)
                            .withFunctionName(FUNCTION_NAME)
                            .withoutProcedureColumnMetaDataAccess()
                            .declareParameters(
                                    new SqlOutParameter(RETURN_KEY, Types.INTEGER),
                                    new SqlOutParameter("o_Data", Types.CLOB),
                                    new SqlParameter("p_Data", Types.VARCHAR));

            Map<String, Object> result = jdbcCall.execute(Map.of("p_Data", xmlData));

            Integer returnValue = (Integer) result.get(RETURN_KEY);

            Clob clobData = (Clob) result.get("o_Data");
            String jsonText = clobToString(clobData);

            log.info(
                    "Function Send_Info executed. Return code: {}, JSON length: {}",
                    returnValue,
                    jsonText != null ? jsonText.length() : 0);

            return MipFunctionResultDto.builder()
                    .returnCode(returnValue)
                    .jsonText(jsonText)
                    .build();
        } catch (Exception e) {
            log.error("Error calling Send_Info functio", e);
            throw new RuntimeException("Failed to call Send_Info Oracle function", e);
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
