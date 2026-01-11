package uz.fido.pfexchange.repository.minyust;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.fido.pfexchange.entity.minyust.WomenChildrenInf;

@Repository
public interface WomenChildrenInfRepository
    extends JpaRepository<WomenChildrenInf, Long> {}
