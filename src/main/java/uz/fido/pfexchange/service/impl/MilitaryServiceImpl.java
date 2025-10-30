package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.military.MilitaryRequestDto;
import uz.fido.pfexchange.dto.military.MilitaryResponseDto;
import uz.fido.pfexchange.exceptioning.RestException;
import uz.fido.pfexchange.repository.CustomQueryRepository;
import uz.fido.pfexchange.service.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilitaryServiceImpl implements MilitaryService {

    private final ObjectMapper objectMapper;
    private final CustomQueryRepository customQueryRepository;
    private final RestClient restClient = RestClient.create();
    private final String URL =
        "http://172.17.1.180:7001/PF/pf/mip/sendPensionerForCheck.jsp";

    @Override
    public MilitaryResponseDto sendRequest(MilitaryRequestDto requestDto) {
        String tokenJson = customQueryRepository.getTokenJson();
        String token = extractAccessToken(tokenJson);
        return callExternalService(requestDto, token);
    }

    private MilitaryResponseDto callExternalService(
        MilitaryRequestDto requestDto,
        String token
    ) {
        try {
            log.info(
                "Calling external service: {} with body: {}",
                URL,
                requestDto
            );

            return restClient
                .post()
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                // .header("Authorization", "Bearer " + token)
                .body(tempBody(requestDto))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                    int statusCode = response.getStatusCode().value();
                    log.error(
                        "4xx error from military service: {}",
                        statusCode
                    );

                    String errorMessage = switch (statusCode) {
                        case 400 -> "Invalid request to military service";
                        case 401 -> "Unauthorized access to military service";
                        case 403 -> "Access forbidden to military service";
                        case 404 -> "Military service endpoint not found";
                        default -> "Military service client error: " +
                        statusCode;
                    };

                    throw RestException.restThrow(
                        ResponseWrapperDto.<Void>builder()
                            .code(Constants.ERROR)
                            .message(errorMessage)
                            .build(),
                        HttpStatus.valueOf(statusCode)
                    );
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, response) -> {
                    int statusCode = response.getStatusCode().value();
                    log.error(
                        "5xx error from military service: {}",
                        statusCode
                    );

                    throw RestException.restThrow(
                        ResponseWrapperDto.<Void>builder()
                            .code(Constants.ERROR)
                            .message("Military service temporarily unavailable")
                            .build(),
                        HttpStatus.BAD_GATEWAY
                    );
                })
                .body(MilitaryResponseDto.class);
        } catch (ResourceAccessException e) {
            log.error("Connection error to {}: {}", URL, e.getMessage(), e);
            throw RestException.restThrow(
                ResponseWrapperDto.<Void>builder()
                    .code(503)
                    .message("Failed to connect to military service")
                    .build(),
                HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    private String extractAccessToken(String json) {
        try {
            return objectMapper.readTree(json).get("access_token").asText();
        } catch (Exception e) {
            return null;
        }
    }

    private String tempBody(MilitaryRequestDto requestDto) {
        return (
            "{" +
            "\"access_token\": {}," +
            "\"end_point_address\": \"https://apimgw.egov.uz:8243/mudofaa/millitary/info/v1\"," +
            "\"is_old\": \"true\"," +
            "\"protocol\": \"https\"," +
            "\"method\": \"POST\"," +
            "\"do_output\": \"true\"," +
            "\"content_type\": \"application/json\"," +
            "\"authorization\": \"Bearer\"," +
            "\"body_type\": \"json\"," +
            "\"data_body\": {" +
            "\"pin\": \"" +
            escapeJson(requestDto.getPin()) +
            "\"," +
            "\"sender_pin\": \"" +
            escapeJson(requestDto.getSenderPin()) +
            "\"," +
            "\"transaction_id\": " +
            requestDto.getTransactionId() +
            "," +
            "\"purpose\": \"" +
            escapeJson(requestDto.getPurpose()) +
            "\"," +
            "\"consent\": \"" +
            escapeJson(requestDto.getConsent()) +
            "\"" +
            "}" +
            "}"
        );
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
