package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.fido.pfexchange.entity.Organization;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    Optional<Organization> findByCoato(String coato);
}
