package uz.fido.pfexchange.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.exception.RestException;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestClientWrapper {

    private final RestClient restClient;
    private final MessageSource messageSource;

    public <T> T get(String uri, String token, String serviceName, Class<T> responseType) {
        return restClient
                .get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (req, res) -> handle4xxError(res, serviceName))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (req, res) -> handle5xxError(res, serviceName))
                .body(responseType);
    }

    public <T> T get(
            String uri,
            String token,
            String serviceName,
            ParameterizedTypeReference<T> responseType) {
        return restClient
                .get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (req, res) -> handle4xxError(res, serviceName))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (req, res) -> handle5xxError(res, serviceName))
                .body(responseType);
    }

    public <T, R> R post(
            String uri, String token, T body, String serviceName, Class<R> responseType) {
        return restClient
                .post()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (req, res) -> handle4xxError(res, serviceName))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (req, res) -> handle5xxError(res, serviceName))
                .body(responseType);
    }

    public <T> T executeGet(
            String uri,
            String serviceName,
            Consumer<RestClient.RequestHeadersUriSpec<?>> requestCustomizer,
            Class<T> responseType) {
        RestClient.RequestHeadersUriSpec<?> spec = restClient.get();
        requestCustomizer.accept(spec);

        return spec.uri(uri)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (req, res) -> handle4xxError(res, serviceName))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (req, res) -> handle5xxError(res, serviceName))
                .body(responseType);
    }

    private void handle4xxError(
            org.springframework.http.client.ClientHttpResponse response, String serviceName)
            throws IOException {

        int statusCode = response.getStatusCode().value();
        log.error("4xx error from {}: {}", serviceName, statusCode);

        String messageKey =
                switch (statusCode) {
                    case 400 -> "error.rest.bad_request";
                    case 401 -> "error.rest.unauthorized";
                    case 403 -> "error.rest.forbidden";
                    case 404 -> "error.rest.not_found";
                    default -> "error.rest.client_error";
                };

        String errorMessage = getMessage(messageKey, serviceName, statusCode);

        throw RestException.restThrow(
                ResponseWrapperDto.<Void>builder()
                        .code(Constants.ERROR)
                        .message(errorMessage)
                        .build(),
                HttpStatus.valueOf(statusCode));
    }

    private void handle5xxError(
            org.springframework.http.client.ClientHttpResponse response, String serviceName)
            throws IOException {

        int statusCode = response.getStatusCode().value();
        log.error("5xx error from {}: {}", serviceName, statusCode);

        String errorMessage = getMessage("error.rest.service_unavailable", serviceName);

        throw RestException.restThrow(
                ResponseWrapperDto.<Void>builder()
                        .code(Constants.ERROR)
                        .message(errorMessage)
                        .build(),
                HttpStatus.BAD_GATEWAY);
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
