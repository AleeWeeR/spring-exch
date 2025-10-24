package uz.fido.pfexchange.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import uz.fido.pfexchange.dto.military.MilitaryRequestDto;
import uz.fido.pfexchange.dto.military.MilitaryResponseDto;
import uz.fido.pfexchange.service.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilitaryServiceImpl implements MilitaryService {

    private final RestClient restClient = RestClient.create();
    private final String URL = "http://10.190.24.50/api/pension/v1/person";

    @Override
    public MilitaryResponseDto sendRequest(
        MilitaryRequestDto requestDto,
        String username,
        String password,
        String url
    ) {
        return callExternalService(requestDto, username, password, url);
    }

    private MilitaryResponseDto callExternalService(
        MilitaryRequestDto requestDto,
        String username,
        String password,
        String url
    ) {
        try {
            log.info(
                "Calling external service: {} with body: {}",
                url != null ? url : URL,
                requestDto
            );

            RestClient.RequestBodySpec request = restClient
                .post()
                .uri(url != null ? url : URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);

            if (username != null && password != null) {
                String credentials = username + ":" + password;
                String encodedCredentials = Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8)
                );
                request.header("Authorization", "Basic " + encodedCredentials);
            }

            return request
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
}
