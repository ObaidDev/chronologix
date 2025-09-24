package com.plutus360.chronologix.dao.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.plutus360.chronologix.entities.PasswordResetToken;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Repository
public class PasswordRepo {


    @PersistenceContext
    private EntityManager em ;




    public PasswordResetToken save (PasswordResetToken token) {
        em.persist(token);
        return token;
    }



    public Optional<PasswordResetToken> findByToken(String token) {

        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        try {
            PasswordResetToken prt = em.createQuery("SELECT p FROM PasswordResetToken p WHERE p.token = :token", PasswordResetToken.class)
                    .setParameter("token", token)
                    .getSingleResult();
            return Optional.of(prt);
        } catch (Exception e) {
            log.error("Error finding PasswordResetToken by token", e);
            return Optional.empty();
        }
    }
    
}
