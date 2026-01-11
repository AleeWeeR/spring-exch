package uz.fido.pfexchange.repository.minyust;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uz.fido.pfexchange.entity.minyust.Women;
import uz.fido.pfexchange.utils.MinyustFamilyStatus;

@Repository
public interface WomenRepository extends JpaRepository<Women, Long> {
    @Query(
        value = """
        Select * From (
            Select *
            From Pf_Women
            Where status = :status
            Order By id
        )
        Where RowNum <= :limit
        """,
        nativeQuery = true
    )
    List<Women> findByStatusWithLimit(
        @Param("status") String status,
        @Param("limit") int limit
    );

    @Query("Select Count(p) From Women p Where p.status = 'READY'")
    long countUnprocessed();

    @Modifying
    @Query(
        """
        Update Women p
        Set p.status = :newStatus, p.retryCount = p.retryCount + 1
        Where p.status = :oldStatus
        And p.requestDate < :threshold
        And p.retryCount < :maxRetries
        """
    )
    int resetStuckRecords(
        @Param("oldStatus") MinyustFamilyStatus oldStatus,
        @Param("newStatus") MinyustFamilyStatus newStatus,
        @Param("threshold") LocalDateTime threshold,
        @Param("maxRetries") int maxRetries
    );

    @Query(
        """
        Select p.status as status, Count(p) as count
        From Women p
        Group By p.status
        """
    )
    List<StatusCount> getStatusCountsRaw();

    default Map<String, Long> getStatusCounts() {
        return getStatusCountsRaw()
            .stream()
            .collect(
                Collectors.toMap(
                    sc -> sc.getStatus().name(),
                    StatusCount::getCount
                )
            );
    }

    interface StatusCount {
        MinyustFamilyStatus getStatus();
        Long getCount();
    }

    Women findByPinpp(String pinpp);
}
