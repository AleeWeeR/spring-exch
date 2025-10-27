package uz.fido.pfexchange.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import uz.fido.pfexchange.dto.military.MilitaryRequestDto;
import uz.fido.pfexchange.dto.military.MilitaryResponseDto;
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
        "https://apimgw.egov.uz:8243/mudofaa/millitary/info/v1";

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
                .header("Authorization", "Bearer " + token)
                .body(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                    log.error("4xx error: {}", response.getStatusCode());
                    throw new IllegalArgumentException(
                        "Client error: " + response.getStatusCode()
                    );
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, response) -> {
                    log.error("5xx error: {}", response.getStatusCode());
                    throw new IllegalStateException(
                        "Server error: " + response.getStatusCode()
                    );
                })
                .body(MilitaryResponseDto.class);
        } catch (ResourceAccessException e) {
            log.error("I/O error connecting to {}: {}", URL, e.getMessage(), e);
            throw new RuntimeException(
                "Failed to connect to external service",
                e
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
}
