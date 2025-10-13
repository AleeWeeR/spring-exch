package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.fido.pfexchange.entity.PfWomenChildrenInf;

@Repository
public interface PfWomenChildrenInfRepository extends JpaRepository<PfWomenChildrenInf, Long> {

    @Query(value = """
            SELECT COUNT(*) FROM Pf_Case_Docs t 
            WHERE t.Doc_Type = '10' And t.Application_Id = :applicationId""",
            nativeQuery = true)
    Long countCaseDocsByClause(@Param("applicationId") Long applicationId);

}
