package com.plutus360.chronologix.dao.repositories;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plutus360.chronologix.dao.interfaces.BaseDao;
import com.plutus360.chronologix.entities.Device;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Query;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;



@Data
@Slf4j
@Repository
public class DeviceRepo implements BaseDao<Device , Long>{

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private  int batchSize ;

    private static final String KEY_NAME_OF_TELEMETRY_DATA = "payload" ;


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


    public List<Map<String, Object>> findDevicesWithJsonbSelection(
        List<Long> ids, 
        List<String> payloadFields,
        OffsetDateTime from,
        OffsetDateTime to) {

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // Validate field names to prevent SQL injection
        List<String> validatedFields = validatePayloadFields(payloadFields);

        String sql = buildSelectQuery(validatedFields, from, to);
        
        log.info("Executing SQL: {}", sql);
        log.info("With parameters - IDs: {}, From: {}, To: {}", ids, from, to);
        
        Query query = em.createNativeQuery(sql);
        query.setParameter("ids", ids.toArray(new Long[0]));
        if (from != null && to != null) {
            query.setParameter("from", from);
            query.setParameter("to", to);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        return mapResultsToDeviceData(results);
    }


    /*
     * private methods Helper for JsonB selection .
     */
    
    private List<String> validatePayloadFields(List<String> payloadFields) {
        if (payloadFields == null || payloadFields.isEmpty()) {
            return payloadFields;
        }
        
        return payloadFields.stream()
            .filter(field -> field != null && !field.trim().isEmpty())
            // Allow dots, letters, numbers, underscores since your fields use dot notation
            .filter(field -> field.matches("^[a-zA-Z_][a-zA-Z0-9_\\.]*$"))
            .distinct()
            .toList();
    }

    private String buildSelectQuery(List<String> payloadFields, OffsetDateTime from, OffsetDateTime to) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT d.id, d.source_device_id, d.device_name, d.ident, d.event_time");
        
        if (payloadFields != null && !payloadFields.isEmpty()) {
            sql.append(", jsonb_build_object(");
            for (int i = 0; i < payloadFields.size(); i++) {
                if (i > 0) sql.append(", ");
                // Use -> instead of ->> to preserve data types
                sql.append("'").append(payloadFields.get(i)).append("', ")
                .append("d.payload->").append("'").append(payloadFields.get(i)).append("'");
            }
            sql.append(") as selected_payload");
        } else {
            sql.append(", d.payload as selected_payload");
        }
        
        sql.append(" FROM devices d WHERE d.source_device_id = ANY(:ids)");
        
        if (from != null && to != null) {
            sql.append(" AND d.event_time BETWEEN :from AND :to");
        }
        
        sql.append(" ORDER BY d.id");
        
        return sql.toString();
    }

    private List<Map<String, Object>> mapResultsToDeviceData(List<Object[]> results) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        return results.stream().map(row -> {
            Map<String, Object> deviceData = new HashMap<>();
            deviceData.put("id", row[0]);
            deviceData.put("sourceDeviceId", row[1]);
            deviceData.put("deviceName", row[2]);
            deviceData.put("ident", row[3]);
            deviceData.put("serverTimestamp", row[4]);
            
            // Handle payload object
            Object payloadObj = row[5];
            if (payloadObj instanceof String payloadStr) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payloadMap = objectMapper.readValue(payloadStr, Map.class);
                    deviceData.put(KEY_NAME_OF_TELEMETRY_DATA, payloadMap);
                } catch (Exception e) {
                    log.error("Failed to parse JSON payload: {}", payloadObj, e);
                    deviceData.put(KEY_NAME_OF_TELEMETRY_DATA, payloadObj);
                }
            } else {
                deviceData.put(KEY_NAME_OF_TELEMETRY_DATA, payloadObj);
            }
            
            return deviceData;
        }).toList();
    }
}
