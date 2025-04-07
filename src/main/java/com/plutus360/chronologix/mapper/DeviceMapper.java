package com.plutus360.chronologix.mapper;


import java.util.List;

import org.springframework.stereotype.Component;

import com.plutus360.chronologix.dtos.DeviceRequest;
import com.plutus360.chronologix.dtos.DeviceResponse;
import com.plutus360.chronologix.entities.Device;

import lombok.extern.slf4j.Slf4j;



@Slf4j
@Component
public class DeviceMapper {








    public List<DeviceResponse> toDeviceResponseList(List<Device> devices) {

        log.debug("Device :{} " , devices);

        List<DeviceResponse> deviceResponses = devices.stream()
                                            .map(this::toDeviceResponse)
                                            .toList() ;
        
        log.debug("group responses :{} " , deviceResponses);
        return deviceResponses;
    }



    public List<Device> toDeviceList(List<DeviceRequest> deviceRequests) {
        return deviceRequests.stream()
                .map(this::toDevice)
                .toList();
    }




    public DeviceResponse toDeviceResponse(Device device) {
        return DeviceResponse.builder()
                .deviceName(device.getDeviceName())
                .ident(device.getIdent())
                .deviceId(device.getSourceDeviceId())
                .serverTimestamp(device.getServerTimestamp())
                .payload(device.getPayload())
                .build();
    }



    public Device toDevice(DeviceRequest deviceRequest) {
        return Device.builder()
                .deviceName(deviceRequest.getDeviceName())
                .ident(deviceRequest.getIdent())
                .sourceDeviceId(deviceRequest.getDeviceId())
                .serverTimestamp(deviceRequest.getServerTimestamp())
                .payload(deviceRequest.getPayload())
                .build();
    }
    
}
