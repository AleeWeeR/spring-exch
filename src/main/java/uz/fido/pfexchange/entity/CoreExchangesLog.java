package uz.fido.pfexchange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CORE_EXCHANGES_LOG")
public class CoreExchangesLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "core_exchanges_log_gen")
    @SequenceGenerator(
            name = "core_exchanges_log_gen",
            sequenceName = "CORE_EXCHANGES_LOG_SEQ",
            allocationSize = 1)
    @Column(name = "LOG_ID")
    private Long logId;

    @Column(name = "CORRELATION_ID", nullable = false, length = 36)
    private String correlationId;

    @Column(name = "DIRECTION", nullable = false, length = 3)
    private String direction;

    @Column(name = "HTTP_METHOD", length = 10)
    private String httpMethod;

    @Column(name = "ENDPOINT", length = 500)
    private String endpoint;

    @Column(name = "QUERY_PARAMS", length = 2000)
    private String queryParams;

    @Lob
    @Column(name = "REQUEST_HEADERS")
    private String requestHeaders;

    @Lob
    @Column(name = "REQUEST_BODY")
    private String requestBody;

    @Column(name = "HTTP_STATUS")
    private Integer httpStatus;

    @Lob
    @Column(name = "RESPONSE_BODY")
    private String responseBody;

    @Column(name = "REMOTE_IP", length = 45)
    private String remoteIp;

    @Column(name = "EXTERNAL_SYSTEM", length = 100)
    private String externalSystem;

    @Column(name = "USER_ID", length = 100)
    private String userId;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    @Column(name = "FINISHED_AT")
    private LocalDateTime finishedAt;

    @Column(name = "DURATION_MS")
    private Long durationMs;

    @Column(name = "ERROR_MESSAGE", length = 4000)
    private String errorMessage;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
