package uz.fido.pfexchange.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import uz.fido.pfexchange.dto.log.LogDto;
import uz.fido.pfexchange.dto.log.LogFilterRequest;
import uz.fido.pfexchange.dto.log.LogStatsDto;
import uz.fido.pfexchange.entity.CoreExchangesLog;
import uz.fido.pfexchange.mapper.LogMapper;
import uz.fido.pfexchange.repository.CoreExchangesLogRepository;
import uz.fido.pfexchange.service.LogQueryService;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LogQueryServiceImpl implements LogQueryService {

    private final EntityManager entityManager;
    private final CoreExchangesLogRepository repository;

    @Override
    public Page<LogDto> findLogs(LogFilterRequest filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        int startRow = page * size + 1;
        int endRow = (page + 1) * size;

        StringBuilder whereClause = new StringBuilder("Where 1=1");
        Map<String, Object> params = new HashMap<>();

        if (hasValue(filter.getCorrelationId())) {
            whereClause.append(" And Correlation_Id = :correlationId");
            params.put("correlationId", filter.getCorrelationId());
        }
        if (hasValue(filter.getDirection())) {
            whereClause.append(" And Direction = :direction");
            params.put("direction", filter.getDirection());
        }
        if (hasValue(filter.getEndpoint())) {
            whereClause.append(" And Lower(Endpoint) Like :endpoint");
            params.put("endpoint", "%" + filter.getEndpoint().toLowerCase() + "%");
        }
        if (filter.getHttpStatus() != null) {
            whereClause.append(" And Http_Status = :httpStatus");
            params.put("httpStatus", filter.getHttpStatus());
        }
        if (hasValue(filter.getExternalSystems())) {
            String[] systems = filter.getExternalSystems().split(",");
            String inClause =
                    String.join(
                            ",",
                            Arrays.stream(systems)
                                    .filter(s -> !s.isBlank())
                                    .map(s -> "'" + s + "'")
                                    .toArray(String[]::new));
            whereClause.append(" And External_System In (" + inClause + ")");
        }
        if (filter.getStartDate() != null) {
            whereClause.append(" And Created_At >= :startDate");
            params.put("startDate", filter.getStartDate());
        }
        if (filter.getEndDate() != null) {
            whereClause.append(" And Created_At <= :endDate");
            params.put("endDate", filter.getEndDate());
        }
        if (hasValue(filter.getExternalSystemsExclude())) {
            String[] excludedSystems = filter.getExternalSystemsExclude().split(",");
            String notInClause =
                    String.join(
                            ",",
                            Arrays.stream(excludedSystems)
                                    .filter(s -> !s.isBlank())
                                    .map(s -> "'" + s + "'")
                                    .toArray(String[]::new));
            whereClause.append(" And External_System Not In (" + notInClause + ")");
        }
        if (Boolean.TRUE.equals(filter.getErrorsOnly())) {
            whereClause.append(" And Http_Status >= 400");
        }

        // Count query
        String countSql = "Select Count(*) From Core_Exchanges_Log " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        params.forEach(countQuery::setParameter);
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
        }

        // Data query - Oracle 11g ROWNUM pagination (NO OFFSET/FETCH)
        String dataSql =
                "Select * From ("
                        + "Select a.*, ROWNUM rnum From ("
                        + "Select * From Core_Exchanges_Log "
                        + whereClause
                        + " Order By Created_At Desc"
                        + ") a Where RowNum <= :endRow"
                        + ") Where rnum >= :startRow";

        Query dataQuery = entityManager.createNativeQuery(dataSql, CoreExchangesLog.class);
        params.forEach(dataQuery::setParameter);
        dataQuery.setParameter("startRow", startRow);
        dataQuery.setParameter("endRow", endRow);

        @SuppressWarnings("unchecked")
        List<CoreExchangesLog> results = dataQuery.getResultList();

        List<LogDto> dtos = results.stream().map(LogMapper::toDto).toList();

        return new PageImpl<>(dtos, PageRequest.of(page, size), total);
    }

    @Override
    public Optional<LogDto> findById(Long id) {
        return repository.findById(id).map(LogMapper::toDto);
    }

    @Override
    public List<LogDto> findByCorrelationId(String correlationId) {
        return repository.findByCorrelationIdOrderByStartedAt(correlationId).stream()
                .map(LogMapper::toDto)
                .toList();
    }

    @Override
    public LogStatsDto getStats(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);

        String sql =
                "Select "
                        + "Count(*) As Total, "
                        + "Sum(Case When Http_Status >= 400 Then 1 Else 0 End) As Errors, "
                        + "AVG(Duration_Ms) As Avg_Duration "
                        + "From Core_Exchanges_Log "
                        + "Where Created_At > :since";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("since", since);

        Object[] result = (Object[]) query.getSingleResult();

        return LogStatsDto.builder()
                .totalRequests(result[0] != null ? ((Number) result[0]).longValue() : 0)
                .errorCount(result[1] != null ? ((Number) result[1]).longValue() : 0)
                .avgDurationMs(result[2] != null ? ((Number) result[2]).longValue() : 0)
                .periodHours(hours)
                .build();
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
