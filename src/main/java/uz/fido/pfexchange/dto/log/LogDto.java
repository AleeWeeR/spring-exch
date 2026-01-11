package uz.fido.pfexchange.dto.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogDto {

    private Long logId;
    private String correlationId;
    private String direction;
    private String httpMethod;
    private String endpoint;
    private String queryParams;
    private String requestHeaders;
    private String requestBody;
    private Integer httpStatus;
    private String responseBody;
    private String remoteIp;
    private String externalSystem;
    private String userId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationMs;
    private String errorMessage;
    private LocalDateTime createdAt;
}
