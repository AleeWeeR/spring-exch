package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.fido.pfexchange.entity.PfSExchangeStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PfSExchangeStatusRepository extends JpaRepository<PfSExchangeStatus, Integer> {

    Optional<PfSExchangeStatus> findByCode(String code);

    List<PfSExchangeStatus> findAllBy();
}
