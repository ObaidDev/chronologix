package com.plutus360.chronologix.integration.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;


import com.plutus360.chronologix.dao.repositories.DeviceRepo;
import com.plutus360.chronologix.entities.Device;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;



@DisabledInAotMode
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class DeviceRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DeviceRepo deviceRepo;

    @PersistenceContext
    private EntityManager entityManager;

    private Device testDevice1;
    private Device testDevice2;
    private OffsetDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = OffsetDateTime.now(ZoneOffset.UTC);
        
        // Create test payload with IoT sensor data
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("position.altitude", 297);
        payload1.put("position.latitude", 12.124405);
        payload1.put("position.longitude", 15.033118);
        payload1.put("position.valid", true);
        payload1.put("movement.status", false);
        payload1.put("battery.voltage", 4.123);

        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("position.altitude", 315);
        payload2.put("position.latitude", 12.125000);
        payload2.put("position.longitude", 15.034000);
        payload2.put("position.valid", true);
        payload2.put("movement.status", true);
        payload2.put("battery.voltage", 4.056);

        testDevice1 = Device.builder()
            .sourceDeviceId(5842962L)
            .deviceName("TestDevice1")
            .ident("TEST001")
            .serverTimestamp(testTime.minusMinutes(30))
            .payload(payload1)
            .build();

        testDevice2 = Device.builder()
            .sourceDeviceId(5842962L)
            .deviceName("TestDevice2")
            .ident("TEST002")
            .serverTimestamp(testTime.minusMinutes(15))
            .payload(payload2)
            .build();

        // Persist test data using EntityManager
        entityManager.persist(testDevice1);
        entityManager.persist(testDevice2);
        entityManager.flush();
    }

    @Test
    void testInsertInBatch() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("temperature", 25.5);
        payload.put("humidity", 60.0);

        Device device1 = Device.builder()
            .sourceDeviceId(1001L)
            .deviceName("BatchDevice1")
            .ident("BATCH001")
            .serverTimestamp(testTime)
            .payload(payload)
            .build();

        Device device2 = Device.builder()
            .sourceDeviceId(1002L)
            .deviceName("BatchDevice2")
            .ident("BATCH002")
            .serverTimestamp(testTime)
            .payload(payload)
            .build();

        List<Device> devicesToInsert = Arrays.asList(device1, device2);

        // Act
        List<Device> result = deviceRepo.insertInBatch(devicesToInsert);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isNotNull();
        assertThat(result.get(1).getId()).isNotNull();
    }

    @Test
    void testFindDevicesWithJsonbSelection_specificFields() {
        // Arrange
        List<Long> ids = Arrays.asList(5842962L);
        List<String> fields = Arrays.asList("position.altitude", "movement.status", "battery.voltage");
        OffsetDateTime from = testTime.minusHours(1);
        OffsetDateTime to = testTime.plusHours(1);

        // Act
        List<Map<String, Object>> result = deviceRepo.findDevicesWithJsonbSelection(ids, fields, from, to);

        // Assert
        assertThat(result).hasSize(2);
        
        Map<String, Object> firstDevice = result.get(0);
        assertThat(firstDevice).containsKeys("id", "sourceDeviceId", "deviceName", "ident", "serverTimestamp", "payload");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) firstDevice.get("payload");
        assertThat(payload).containsOnlyKeys("position.altitude", "movement.status", "battery.voltage");

        // Verify data types and not just string values
        assertThat(payload.get("position.altitude")).isInstanceOf(Number.class); // Could be Integer, Double, etc.
        assertThat(payload.get("movement.status")).isInstanceOf(Boolean.class); // If this should be a string
        assertThat(payload.get("battery.voltage")).isInstanceOf(Number.class);
    }

    @Test
    void testFindDevicesWithJsonbSelection_allFields() {
        // Arrange
        List<Long> ids = Arrays.asList(5842962L);
         OffsetDateTime from = testTime.minusHours(1);
        OffsetDateTime to = testTime.plusHours(1);

        // Act - passing null for payloadFields should return all fields
        List<Map<String, Object>> result = deviceRepo.findDevicesWithJsonbSelection(ids, null, from, to);

        // Assert
        assertThat(result).hasSize(2);
        
        Map<String, Object> firstDevice = result.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) firstDevice.get("payload");
        
        // Should contain all original payload fields
        assertThat(payload).containsKeys("position.altitude", "position.latitude", "position.longitude", 
                                        "position.valid", "movement.status", "battery.voltage");
    }

}
