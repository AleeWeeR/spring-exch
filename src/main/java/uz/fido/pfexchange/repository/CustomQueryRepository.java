package uz.fido.pfexchange.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;

import org.springframework.stereotype.Repository;

import uz.fido.pfexchange.dto.PersonActivityDto;
import uz.fido.pfexchange.utils.Utils;

import java.util.List;

@Repository
public class CustomQueryRepository {

    @PersistenceContext private EntityManager entityManager;

    public String getTokenJson() {
        return entityManager
                .createNativeQuery(
                        """
                        Select t.Access_Token
                          From Pf_Access_Token_Oauth2 t
                         Where t.Pf_Access_Token_Id = 1
                           And Rownum = 1\
                        """)
                .getResultList()
                .getFirst()
                .toString();
    }

    public boolean updateApplicationPayStatusCode(Long applicationId, String newPayStatusCode) {
        int rowsAffected =
                entityManager
                        .createNativeQuery(
                                """
                                Update Pf_Applications a
                                   Set a.Pay_Status_Code = :newPayStatusCode
                                 Where a.Application_Id = :applicationId\
                                """)
                        .setParameter("newPayStatusCode", newPayStatusCode)
                        .setParameter("applicationId", applicationId)
                        .executeUpdate();

        return rowsAffected > 0;
    }

    public List<PersonActivityDto> getPersonActivities(Long personId, Long applicationId) {
        @SuppressWarnings("unchecked")
        List<Tuple> results =
                entityManager
                        .createNativeQuery(
                                """
                                Select Acs.Date_Begin,
                                       Acs.Date_End,
                                       Acs.Activity_Code,
                                       Acs.Is_Stuff_Flag
                                  From Pf_Activities Acs
                                 Where Acs.Person_Id = :personId
                                   And Acs.Application_Id = :applicationId\
                                """,
                                Tuple.class)
                        .setParameter("personId", personId)
                        .setParameter("applicationId", applicationId)
                        .getResultList();

        return results.stream()
                .map(
                        tuple ->
                                new PersonActivityDto(
                                        Utils.convertToLocalDate(tuple.get(0)),
                                        Utils.convertToLocalDate(tuple.get(1)),
                                        (String) tuple.get(2),
                                        tuple.get(3) != null ? tuple.get(3).toString() : null))
                .toList();
    }

    public boolean pensTypeExists(String pensType) {
        boolean exists =
                !entityManager
                        .createNativeQuery(
                                """
                                Select 1
                                  From Pf_S_Exchanges_Mip_Paytypes
                                 Where Code = :pensType\
                                """)
                        .setParameter("pensType", pensType)
                        .getResultList()
                        .isEmpty();

        return exists;
    }
}
