package com.plutus360.chronologix.dao.repositories;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.plutus360.chronologix.dao.interfaces.BaseDao;
import com.plutus360.chronologix.entities.IntegrationToken;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Repository
public class IntegrationTokenRepo implements BaseDao<IntegrationToken , Long>{
    
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private  int batchSize ;


    @PersistenceContext
    private EntityManager em ;
    


    @Override
    public List<IntegrationToken> insertInBatch(List<IntegrationToken> entities) {
        if (entities == null || entities.isEmpty()) {
            return entities ;
        }

        for (int i = 0; i < entities.size(); i++) {
            
            em.persist(entities.get(i));

            if (i > 0 && (i + 1) % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }


        // Flush and clear the remaining entities that didn't make up a full batch
        if (entities.size() % batchSize != 0) {
            em.flush();
            em.clear();
        }

        return entities ;
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteByIds'");
    }

    @Override
    public List<IntegrationToken> findByIds(List<Long> ids) {
        
         if (ids == null || ids.isEmpty()) {
            
            return Collections.emptyList();
        }
    
        String jpql = "SELECT d FROM IntegrationToken d WHERE d.id IN :ids";
        
        return em.createQuery(jpql, IntegrationToken.class)
                            .setParameter("ids", ids)
                            .getResultList();
    }

    @Override
    public List<IntegrationToken> findWithPagination(int page, int pageSize) {
        throw new UnsupportedOperationException("Unimplemented method 'findWithPagination'");
    }

    @Override
    public Long count() {
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }

    @Override
    public int updateInBatch(List<Long> ids, IntegrationToken entity) {
        throw new UnsupportedOperationException("Unimplemented method 'updateInBatch'");
    }

    @Override
    public List<IntegrationToken> findByIdsAndTimeRange(List<Long> ids, OffsetDateTime from, OffsetDateTime to) {
        throw new UnsupportedOperationException("Unimplemented method 'findByIdsAndTimeRange'");
    }
    


    public Optional<IntegrationToken> findToken(String tokenHash) {
        
        log.info("🗯️ Finding token with hash: {} in database level 🗯️", tokenHash);

        if (tokenHash == null || tokenHash.isEmpty()) {
            return Optional.empty();
        }
        
        String jpql = "SELECT d FROM IntegrationToken d WHERE d.tokenHash = :tokenHash";
        
        try {
            IntegrationToken token = em.createQuery(jpql, IntegrationToken.class)
                    .setParameter("tokenHash", tokenHash)
                    .getSingleResult();
            return Optional.of(token);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }



    public boolean deleteToken (String token) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteToken'");

    }

}
