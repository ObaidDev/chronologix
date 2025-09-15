package com.plutus360.chronologix.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plutus360.chronologix.dao.repositories.DeviceRepo;
import com.plutus360.chronologix.dtos.DeviceRequest;
import com.plutus360.chronologix.dtos.DeviceResponse;
import com.plutus360.chronologix.entities.Device;
import com.plutus360.chronologix.mapper.DeviceMapper;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class DeviceService {


    private DeviceMapper deviceMapper;

    private DeviceRepo deviceRepo;



    @Autowired
    DeviceService(
        DeviceMapper deviceMapper,
        DeviceRepo deviceRepo
    ) {
        this.deviceMapper = deviceMapper;
        this.deviceRepo = deviceRepo;
    }




    public List<DeviceResponse> createEntities(List<DeviceRequest> deviceRequests) {

        log.info("Creating groups in batch...");
        

        List<Device> devices = deviceRepo.insertInBatch(deviceMapper.toDeviceList(deviceRequests) ) ;

        log.debug("service : groups : {}" , devices);

        List<DeviceResponse> deviceResponses = deviceMapper.toDeviceResponseList(devices);

        log.debug("service : DeviceResponses : {}" , deviceResponses);

        return deviceResponses ;
        
    }



    public List<DeviceResponse> findByIdsAndTimeRange(List<Long> ids , long from, long to) {
        
        OffsetDateTime fromTime = Instant.ofEpochSecond(from).atOffset(ZoneOffset.UTC);
        OffsetDateTime toTime = Instant.ofEpochSecond(to).atOffset(ZoneOffset.UTC);

       return deviceMapper.toDeviceResponseList(deviceRepo.findByIdsAndTimeRange(ids, fromTime, toTime));
        
    }


    public List<Map<String , Object>> findByIdsAndTimeRangeWithFieldsSelection(
        List<Long> ids, long from, long to, List<String> payloadFields
    ) {

        // OffsetDateTime fromTime = Instant.ofEpochSecond(from).atOffset(ZoneOffset.UTC);
        // OffsetDateTime toTime = Instant.ofEpochSecond(to).atOffset(ZoneOffset.UTC);


        Instant fromTime = Instant.ofEpochSecond(from);
        Instant toTime = Instant.ofEpochSecond(to);


        return deviceRepo.findDevicesWithJsonbSelection(ids, payloadFields, fromTime, toTime);
    }
    
}