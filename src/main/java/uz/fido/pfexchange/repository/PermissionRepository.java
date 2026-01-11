package uz.fido.pfexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uz.fido.pfexchange.entity.Permission;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByIsActiveFlag(String isActiveFlag);

    @Modifying
    @Query(
            """
            Update Permission p Set p.isActiveFlag = :flag, p.lastUpdateDate = Sysdate
            Where p.code In :codes
            """)
    int updateActiveFlagByCodes(@Param("codes") List<String> codes, @Param("flag") String flag);

    @Modifying
    @Query(
            """
            Update Permission p Set p.name = :name, p.lastUpdateDate = Sysdate
            Where p.code = :code
            """)
    int updateNameByCode(@Param("code") String code, @Param("name") String name);
}
