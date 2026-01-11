package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uz.fido.pfexchange.entity.PushInfo;

@Repository
public interface PushInfoRepository extends JpaRepository<PushInfo, Long> {}
