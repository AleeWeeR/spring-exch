package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.fido.pfexchange.entity.PfSExchangeDocTypes;

import java.util.Optional;

@Repository
public interface PfSExchangeDocTypesRepository extends JpaRepository<PfSExchangeDocTypes, String> {

    Optional<PfSExchangeDocTypes> getPfSExchangeDocTypesByCodeAndIsActiveFlag(String code, String isActiveFlag);
}
