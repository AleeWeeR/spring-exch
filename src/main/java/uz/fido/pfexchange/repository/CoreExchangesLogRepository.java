package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uz.fido.pfexchange.entity.CoreExchangesLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CoreExchangesLogRepository
        extends JpaRepository<CoreExchangesLog, Long>, JpaSpecificationExecutor<CoreExchangesLog> {

    List<CoreExchangesLog> findByCorrelationIdOrderByStartedAt(String correlationId);

    Long countByCreatedAtAfter(LocalDateTime since);

    Long countByCreatedAtAfterAndHttpStatusGreaterThanEqual(LocalDateTime since, Integer status);

    @Query("Select Avg(c.durationMs) From CoreExchangesLog c Where c.createdAt > :since")
    Double avgDurationByCreatedAtAfter(@Param("since") LocalDateTime since);
}
