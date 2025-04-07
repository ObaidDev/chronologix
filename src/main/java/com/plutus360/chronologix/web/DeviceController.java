package com.plutus360.chronologix.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    
}
