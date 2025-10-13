package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.fido.pfexchange.entity.PfWomen;

import java.util.List;

public interface PfWomenRepository extends JpaRepository<PfWomen, Long> {

    @Query(value = """
        SELECT * FROM (
            SELECT * 
            FROM pf_women 
            WHERE status = :status 
            ORDER BY id
        ) 
        WHERE ROWNUM <= :limit
        """,
            nativeQuery = true)
    List<PfWomen> findByStatusWithLimit(
            @Param("status") String status,
            @Param("limit") int limit
    );

    @Query("SELECT COUNT(p) FROM PfWomen p WHERE p.status = 'READY'")
    long countUnprocessed();

    PfWomen findByPinpp(String pinpp);
}