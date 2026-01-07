package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.fido.pfexchange.entity.PfMibCancelRel;

import java.util.List;
import java.util.Optional;

public interface PfMibCancelRelRepository extends JpaRepository<PfMibCancelRel, Long> {

    Optional<PfMibCancelRel> findByExternalId(Long externalId);

    List<PfMibCancelRel> findByIsSentAndIsCancelled(String isSent, String isCancelled);

    @Query("SELECT m FROM PfMibCancelRel m WHERE m.pinpp = :pinpp AND m.isSent = 'N'")
    List<PfMibCancelRel> findUnsentByPinpp(@Param("pinpp") String pinpp);
}
