package uz.fido.pfexchange.dto.log;

import lombok.Getter;
import lombok.Setter;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class LogFilterRequest {

    private Integer page = 0;
    private Integer size = 20;
    private String correlationId;
    private String direction;
    private String endpoint;
    private Integer httpStatus;
    private String externalSystems;
    private String externalSystemsExclude;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private Boolean errorsOnly;
}
