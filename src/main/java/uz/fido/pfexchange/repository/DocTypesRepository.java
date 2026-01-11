package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.fido.pfexchange.entity.DocTypes;

import java.util.Optional;

@Repository
public interface DocTypesRepository extends JpaRepository<DocTypes, String> {

    Optional<DocTypes> getDocTypesByCodeAndIsActiveFlag(String code, String isActiveFlag);
}
