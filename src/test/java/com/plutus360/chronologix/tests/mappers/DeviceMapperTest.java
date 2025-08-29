package com.plutus360.chronologix.tests.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.plutus360.chronologix.dtos.DeviceRequest;
import com.plutus360.chronologix.dtos.DeviceResponse;
import com.plutus360.chronologix.entities.Device;
import com.plutus360.chronologix.mapper.DeviceMapper;

/**
 * Unit tests for DeviceMapper
 * Tests entity to DTO mapping functionality - NO MOCKS NEEDED
 */
@DisplayName("DeviceMapper Unit Tests")
class DeviceMapperTest {

    private DeviceMapper deviceMapper;

    @BeforeEach
    void setUp() {
        // Create real instance - no mocking needed
        deviceMapper = new DeviceMapper();
    }

    @Test
    @DisplayName("Should map Device entity to DeviceResponse correctly")
    void should_MapDeviceEntityToResponse_When_ValidEntityProvided() {
        // Arrange
        Device device = createSampleDevice();

        // Act
        DeviceResponse deviceResponse = deviceMapper.toDeviceResponse(device);

        // Assert
        assertThat(deviceResponse).isNotNull();
        assertThat(deviceResponse.getDeviceName()).isEqualTo(device.getDeviceName());
        assertThat(deviceResponse.getIdent()).isEqualTo(device.getIdent());
        assertThat(deviceResponse.getDeviceId()).isEqualTo(device.getSourceDeviceId());
        assertThat(deviceResponse.getServerTimestamp()).isEqualTo(device.getServerTimestamp());
        assertThat(deviceResponse.getPayload()).isEqualTo(device.getPayload());
    }

    @Test
    @DisplayName("Should map DeviceRequest to Device entity correctly")
    void should_MapDeviceRequestToEntity_When_ValidRequestProvided() {
        // Arrange
        DeviceRequest deviceRequest = createSampleDeviceRequest();

        // Act
        Device device = deviceMapper.toDevice(deviceRequest);

        // Assert
        assertThat(device).isNotNull();
        assertThat(device.getDeviceName()).isEqualTo(deviceRequest.getDeviceName());
        assertThat(device.getIdent()).isEqualTo(deviceRequest.getIdent());
        assertThat(device.getSourceDeviceId()).isEqualTo(deviceRequest.getDeviceId());
        assertThat(device.getServerTimestamp()).isEqualTo(deviceRequest.getServerTimestamp());
        assertThat(device.getPayload()).isEqualTo(deviceRequest.getPayload());
    }

    @Test
    @DisplayName("Should map list of Device entities to list of DeviceResponses correctly")
    void should_MapDeviceEntityListToResponseList_When_ValidEntityListProvided() {
        // Arrange
        List<Device> devices = Arrays.asList(
            createSampleDevice(),
            createSampleDeviceWithIdent("device-002"),
            createSampleDeviceWithIdent("device-003")
        );

        // Act
        List<DeviceResponse> deviceResponses = deviceMapper.toDeviceResponseList(devices);

        // Assert
        assertThat(deviceResponses).isNotNull();
        assertThat(deviceResponses).hasSize(3);
        
        // Verify first device mapping
        DeviceResponse firstResponse = deviceResponses.get(0);
        Device firstDevice = devices.get(0);
        assertThat(firstResponse.getDeviceName()).isEqualTo(firstDevice.getDeviceName());
        assertThat(firstResponse.getIdent()).isEqualTo(firstDevice.getIdent());
        assertThat(firstResponse.getDeviceId()).isEqualTo(firstDevice.getSourceDeviceId());
    }

    @Test
    @DisplayName("Should map list of DeviceRequests to list of Device entities correctly")
    void should_MapDeviceRequestListToEntityList_When_ValidRequestListProvided() {
        // Arrange
        List<DeviceRequest> deviceRequests = Arrays.asList(
            createSampleDeviceRequest(),
            createSampleDeviceRequestWithIdent("device-002"),
            createSampleDeviceRequestWithIdent("device-003")
        );

        // Act
        List<Device> devices = deviceMapper.toDeviceList(deviceRequests);

        // Assert
        assertThat(devices).isNotNull();
        assertThat(devices).hasSize(3);
        
        // Verify first device mapping
        Device firstDevice = devices.get(0);
        DeviceRequest firstRequest = deviceRequests.get(0);
        assertThat(firstDevice.getDeviceName()).isEqualTo(firstRequest.getDeviceName());
        assertThat(firstDevice.getIdent()).isEqualTo(firstRequest.getIdent());
        assertThat(firstDevice.getSourceDeviceId()).isEqualTo(firstRequest.getDeviceId());
    }

    // Helper methods for creating test data

    private Device createSampleDevice() {
        return Device.builder()
                .deviceName("Test Device")
                .ident("device-001")
                .sourceDeviceId(123456L)
                .serverTimestamp(OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, java.time.ZoneOffset.UTC))
                .payload(Collections.singletonMap("temperature", 25.5))
                .build();
    }

    private Device createSampleDeviceWithIdent(String ident) {
        return Device.builder()
                .deviceName("Test Device " + ident)
                .ident(ident)
                .sourceDeviceId(10L)
                .serverTimestamp(OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .payload(Collections.singletonMap("temperature", 25.5))
                .build();
    }

    private DeviceRequest createSampleDeviceRequest() {
        return DeviceRequest.builder()
                .deviceName("Test Device")
                .ident("device-001")
                .deviceId(123456L)
                .serverTimestamp(java.time.OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, java.time.ZoneOffset.UTC))
                .payload(java.util.Collections.singletonMap("temperature", 25.5))
                .build();
    }

    private DeviceRequest createSampleDeviceRequestWithIdent(String ident) {
        return DeviceRequest.builder()
                .deviceName("Test Device " + ident)
                .ident(ident)
                .deviceId(123456L)
                .serverTimestamp(java.time.OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, java.time.ZoneOffset.UTC))
                .payload(java.util.Collections.singletonMap("data", "test"))
                .build();
    }
}