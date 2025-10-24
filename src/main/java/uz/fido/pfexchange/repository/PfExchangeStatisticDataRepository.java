package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.fido.pfexchange.entity.OrgPeriodKey;
import uz.fido.pfexchange.entity.PfExchangeStatisticData;

@Repository
public interface PfExchangeStatisticDataRepository extends JpaRepository<PfExchangeStatisticData, OrgPeriodKey> {
}
