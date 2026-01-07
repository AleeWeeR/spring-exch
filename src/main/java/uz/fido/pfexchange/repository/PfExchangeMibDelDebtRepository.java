package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.fido.pfexchange.entity.PfExchangeMibDelDebt;

import java.util.List;

public interface PfExchangeMibDelDebtRepository extends JpaRepository<PfExchangeMibDelDebt, Long> {

    List<PfExchangeMibDelDebt> findByExternalId(Long externalId);

    Long countByExternalId(Long externalId);

    List<PfExchangeMibDelDebt> findByPinpp(String pinpp);
}
