package uz.fido.pfexchange.service;

import org.springframework.data.domain.Page;

import uz.fido.pfexchange.dto.log.LogDto;
import uz.fido.pfexchange.dto.log.LogFilterRequest;
import uz.fido.pfexchange.dto.log.LogStatsDto;

import java.util.List;
import java.util.Optional;

public interface LogQueryService {

    Page<LogDto> findLogs(LogFilterRequest filter);

    Optional<LogDto> findById(Long id);

    List<LogDto> findByCorrelationId(String correlationId);

    LogStatsDto getStats(int hours);
}
