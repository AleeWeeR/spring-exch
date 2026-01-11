package uz.fido.pfexchange.mapper;

import uz.fido.pfexchange.dto.log.LogDto;
import uz.fido.pfexchange.entity.CoreExchangesLog;

public class LogMapper {

    public static LogDto toDto(CoreExchangesLog entity) {
        if (entity == null) {
            return null;
        }

        return LogDto.builder()
                .logId(entity.getLogId())
                .correlationId(entity.getCorrelationId())
                .direction(entity.getDirection())
                .httpMethod(entity.getHttpMethod())
                .endpoint(entity.getEndpoint())
                .queryParams(entity.getQueryParams())
                .requestHeaders(entity.getRequestHeaders())
                .requestBody(entity.getRequestBody())
                .httpStatus(entity.getHttpStatus())
                .responseBody(entity.getResponseBody())
                .remoteIp(entity.getRemoteIp())
                .externalSystem(entity.getExternalSystem())
                .userId(entity.getUserId())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .durationMs(entity.getDurationMs())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
