package com.plutus360.chronologix.dao.repositories;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.plutus360.chronologix.dao.interfaces.BaseDao;
import com.plutus360.chronologix.entities.Device;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;



@Data
@Slf4j
@Repository
public class DeviceRepo implements BaseDao<Device , Long>{

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private  int batchSize ;


    @PersistenceContext
    private EntityManager em ;


    @Override
    public List<Device> insertInBatch(List<Device> entities) {

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
    public List<Device> findByIds(List<Long> ids) {
       
        throw new UnsupportedOperationException("Unimplemented method 'findByIds'");
    }


    @Override
    public List<Device> findWithPagination(int page, int pageSize) {
       
        throw new UnsupportedOperationException("Unimplemented method 'findWithPagination'");
    }


    @Override
    public Long count() {
       
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }


    @Override
    public int updateInBatch(List<Long> ids, Device entity) {
       
        throw new UnsupportedOperationException("Unimplemented method 'updateInBatch'");
    }


    @Override
    public List<Device> findByIdsAndTimeRange(List<Long> ids, OffsetDateTime from, OffsetDateTime to) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        String jpql = "SELECT d FROM Device d WHERE d.sourceDeviceId IN :ids AND d.serverTimestamp BETWEEN :from AND :to ORDER BY d.id";

        TypedQuery<Device> query = em.createQuery(jpql, Device.class);
        query.setParameter("ids", ids);
        query.setParameter("from", from);
        query.setParameter("to", to);

        return query.getResultList();
    }


    
}
