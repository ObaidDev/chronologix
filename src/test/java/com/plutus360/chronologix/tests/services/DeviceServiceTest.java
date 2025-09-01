package com.plutus360.chronologix.tests.services;




import com.plutus360.chronologix.dao.repositories.DeviceRepo;
import com.plutus360.chronologix.dtos.DeviceRequest;
import com.plutus360.chronologix.dtos.DeviceResponse;
import com.plutus360.chronologix.entities.Device;
import com.plutus360.chronologix.mapper.DeviceMapper;
import com.plutus360.chronologix.service.DeviceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {


    @Mock
    private DeviceMapper deviceMapper;

    @Mock
    private DeviceRepo deviceRepo;

    @InjectMocks
    private DeviceService deviceService;

    private DeviceRequest request;
    private Device device;
    private DeviceResponse response;

    @BeforeEach
    void setup() {
        request = DeviceRequest.builder().deviceName("Device-1").build();
        device = Device.builder().id(1L).deviceName("Device-1").build();
        response = DeviceResponse.builder().deviceId(1L).deviceName("Device-1").build();
    }




    @Test
    void createEntities_ShouldMapRequestSaveAndReturnResponse() {
        // Arrange
        List<DeviceRequest> requests = List.of(request);
        List<Device> devices = List.of(device);
        List<DeviceResponse> responses = List.of(response);

        when(deviceMapper.toDeviceList(requests)).thenReturn(devices);
        when(deviceRepo.insertInBatch(devices)).thenReturn(devices);
        when(deviceMapper.toDeviceResponseList(devices)).thenReturn(responses);

        // Act
        List<DeviceResponse> result = deviceService.createEntities(requests);

        // Assert
        assertThat(result).containsExactly(response);
        verify(deviceMapper).toDeviceList(requests);
        verify(deviceRepo).insertInBatch(devices);
        verify(deviceMapper).toDeviceResponseList(devices);
    }



    @Test
    void findByIdsAndTimeRange_ShouldReturnMappedResponses() {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        long from = 1000L;
        long to = 2000L;

        List<Device> devices = List.of(device);
        List<DeviceResponse> responses = List.of(response);

        when(deviceRepo.findByIdsAndTimeRange(
                eq(ids),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class))
        ).thenReturn(devices);

        when(deviceMapper.toDeviceResponseList(devices)).thenReturn(responses);

        // Act
        List<DeviceResponse> result = deviceService.findByIdsAndTimeRange(ids, from, to);

        // Assert
        assertThat(result).containsExactly(response);
        verify(deviceRepo).findByIdsAndTimeRange(
                eq(ids),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class)
        );
        verify(deviceMapper).toDeviceResponseList(devices);
    }





    @Test
    void findByIdsAndTimeRangeWithFieldsSelection_ShouldReturnRepoResults() {
        // Arrange
        List<Long> ids = List.of(1L);
        long from = 1000L;
        long to = 2000L;
        List<String> fields = List.of("field1", "field2");

        List<Map<String, Object>> repoResult = List.of(Map.of("field1", "value"));

        when(deviceRepo.findDevicesWithJsonbSelection(
                eq(ids),
                eq(fields),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class))
        ).thenReturn(repoResult);

        // Act
        List<Map<String, Object>> result =
                deviceService.findByIdsAndTimeRangeWithFieldsSelection(ids, from, to, fields);

        // Assert
        assertThat(result).isEqualTo(repoResult);
        verify(deviceRepo).findDevicesWithJsonbSelection(
                eq(ids),
                eq(fields),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class)
        );
    }

    
}