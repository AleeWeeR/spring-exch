package uz.fido.pfexchange.service.impl.mib;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import uz.fido.pfexchange.dto.mib.MibCancelDebtRequestDto;
import uz.fido.pfexchange.service.mib.MibCancelDebtService;

/**
 * Service implementation for MIB debt cancellation API proxy
 * This replaces the JSP sendMibCancel.jsp endpoint
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MibCancelDebtServiceImpl implements MibCancelDebtService {

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Value("${mib.pension.cancel-inventory.url:https://pension.mib.uz/cancel-inventory}")
    private String mibCancelUrl;

    @Value("${mib.pension.cancel-inventory.auth:Basic cGVuc2lvbjpxcFtYJDM5JG5bdS5yZS40}")
    private String mibAuthHeader;

    @Override
    public String sendCancelDebtRequest(MibCancelDebtRequestDto payload) {
        log.info("Sending cancel debt request to MIB API: {}", mibCancelUrl);
        log.debug("Payload: {}", payload);

        try {
            // Call MIB API
            ResponseEntity<String> response = restClient
                    .post()
                    .uri(mibCancelUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", mibAuthHeader)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);

            String responseBody = response.getBody();
            log.info("MIB API response received");
            log.debug("Response body: {}", responseBody);

            return responseBody;

        } catch (RestClientResponseException e) {
            // Handle HTTP error responses from MIB
            int statusCode = e.getStatusCode().value();
            String errorBody = e.getResponseBodyAsString();

            log.error("MIB API error response - status: {}, body: {}", statusCode, errorBody);

            // Return error in XML format matching JSP behavior
            return buildErrorXml(statusCode, "Ошибка сети: " + errorBody + " (" + mibCancelUrl + ")");

        } catch (ResourceAccessException e) {
            // Handle network/connection errors
            log.error("Connection error to MIB service: {}", mibCancelUrl, e);

            Throwable rootCause = e.getRootCause();
            String errorMessage = rootCause != null ? rootCause.getMessage() : e.getMessage();

            return buildErrorXml(1, "Ошибка сети: " + errorMessage + " (" + mibCancelUrl + ")");

        } catch (Exception e) {
            // Handle any other errors
            log.error("Unexpected error calling MIB API", e);

            return buildErrorXml(2, "Ошибка сети: " + e.toString());
        }
    }

    /**
     * Build error response in XML format (matching JSP behavior)
     */
    private String buildErrorXml(int resultCode, String resultMessage) {
        return "<result_code>" + resultCode + "</result_code>" +
                "<result_message>" + escapeXml(resultMessage) + "</result_message>";
    }

    /**
     * Escape special characters for XML
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}