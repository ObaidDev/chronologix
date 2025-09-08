package com.plutus360.chronologix.dao.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.plutus360.chronologix.entities.CitusTable;
import com.plutus360.chronologix.entities.DistNode;
import com.plutus360.chronologix.entities.ShardInfo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class DatabaseInfoRepo {


    @PersistenceContext
    private EntityManager em ;





    @SuppressWarnings("unchecked")
    public List<ShardInfo> getAllShards() {

        String sql = "SELECT * FROM citus_shards";
        return em.createNativeQuery(sql, ShardInfo.class)
                 .getResultList();
    }



    @SuppressWarnings("unchecked")
    public List<DistNode> getAllDistNodes() {
        String sql = "SELECT * FROM pg_dist_node";
        return em.createNativeQuery(sql, DistNode.class)
                 .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<CitusTable> getAllCitusTables() {
        String sql = "SELECT * FROM citus_tables";
        return em.createNativeQuery(sql, CitusTable.class)
                 .getResultList();
    }
    
}
