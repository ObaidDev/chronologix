package com.plutus360.chronologix.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plutus360.chronologix.dtos.DeviceRequest;
import com.plutus360.chronologix.dtos.DeviceResponse;
import com.plutus360.chronologix.service.DeviceService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/devices")
@Slf4j
@Validated
public class DeviceController {



    private DeviceService deviceService;


    @Autowired
    DeviceController(
        DeviceService deviceService
    ) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public boolean createDevices(
        @RequestBody @Valid List<DeviceRequest> deviceRequests
    ) {

        deviceService.createEntities(deviceRequests);

        return true;
    }




    @GetMapping("/logs/{ids}")
    public List<DeviceResponse> getDevicesByIdsAndTimeRange(
            @PathVariable List<Long> ids,
            @RequestParam long from,
            @RequestParam long to
    ) {

        

        log.info("Searching devices with IDs: {} from {} to {}", ids, from, to);

        return deviceService.findByIdsAndTimeRange(ids, from, to);
    }



    @GetMapping("/logs/fields/{ids}")
    public List<Map<String, Object>> getDevicesByIdsAndTimeRangeWithFieldsSelection(
            @PathVariable List<Long> ids,
            @RequestParam long from,
            @RequestParam long to ,
            @RequestParam List<String> fields
    ) {

        log.info("Searching devices with IDs: {} from {} to {} with fields: {}", ids, from, to, fields);

        return deviceService.findByIdsAndTimeRangeWithFieldsSelection(ids, from, to, fields);
    }

}
