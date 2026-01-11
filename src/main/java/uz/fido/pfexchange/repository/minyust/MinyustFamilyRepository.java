package uz.fido.pfexchange.repository.minyust;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.fido.pfexchange.entity.minyust.MinyustFamily;
import java.util.List;

@Repository
public interface MinyustFamilyRepository extends JpaRepository<MinyustFamily, Long> {
    
    @Query(value = "SELECT * FROM PF_EXCHANGE_MINYUST_FAMILY WHERE status = :status AND ROWNUM <= :limit", 
           nativeQuery = true)
    List<MinyustFamily> findByStatusWithLimit(@Param("status") String status, @Param("limit") int limit);
    
    @Query("SELECT COUNT(e) FROM MinyustFamily e WHERE e.status.code = 'RE'")
    long countUnprocessed();
    
    List<MinyustFamily> findByPinpp(String pinpp);
}