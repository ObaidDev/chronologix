package com.plutus360.chronologix.dao.interfaces;

import java.util.List;

public interface BaseDao <T, I>{

    List<T> insertInBatch(List<T> entities);
    
    int deleteByIds(List<I> ids);
    
    List<T> findByIds(List<I> ids);
    
    List<T> findWithPagination(int page, int pageSize);
    
    Long count();

    public int updateInBatch(List<I> ids, T entity) ;
}