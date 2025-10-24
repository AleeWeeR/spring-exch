package uz.fido.pfexchange.repository.minyust;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.fido.pfexchange.entity.minyust.PfExchangeMinyustFamilyInf;

@Repository
public interface PfExchangeMinyustFamilyInfRepository
    extends JpaRepository<PfExchangeMinyustFamilyInf, Long> {
    @Query(
        value = """
        Select Mfi.Pinpp
        From Pf_Exchange_Minyust_Family Mf
        Join Pf_Exchange_Minyust_Family_Inf Mfi
            On Mfi.Query_Family_Id = Mf.Query_Family_Id
           And Mfi.Mother_Pin = Mf.Pinpp
        Where Mf.Status = '06'
            And Mf.Pinpp = :pinpp
            And Mf.Query_Family_Id = (
                  Select Max(Query_Family_Id)
                  From Pf_Exchange_Minyust_Family
                  Where Pinpp = :pinpp
                    And Status = '06'
              )""",
        nativeQuery = true
    )
    Optional<List<String>> getLatestMinyustByPinpp(
        @Param("pinpp") String pinpp
    );
}
