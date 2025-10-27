package uz.fido.pfexchange.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class CustomQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public String getTokenJson() {
        return entityManager
            .createNativeQuery(
                """
                Select t.Access_Token
                  From Pf_Access_Token_Oauth2 t
                 Where t.Pf_Access_Token_Id = 1
                   And Rownum = 1"""
            )
            .getResultList()
            .getFirst()
            .toString();
    }
}
