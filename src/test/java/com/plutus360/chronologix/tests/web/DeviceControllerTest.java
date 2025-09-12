package com.plutus360.chronologix.tests.web;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plutus360.chronologix.dtos.DeviceRequest;
import com.plutus360.chronologix.dtos.DeviceResponse;
import com.plutus360.chronologix.service.DeviceService;
import com.plutus360.chronologix.web.DeviceController;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DeviceControllerTest {


    private MockMvc mockMvc;


    @Mock
    private DeviceService deviceService;


    @InjectMocks
    private DeviceController deviceController ;


    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());



    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(deviceController).build();
    }



    @Test
    void createDevicesValidRequestReturns200() throws Exception {

        DeviceRequest request = DeviceRequest.builder()
                                                .deviceId(1L)
                                                .ident("ident-test")
                                                .serverTimestamp(OffsetDateTime.now())
                                                .deviceName("Device 1")
                                                .build();


        DeviceResponse response = DeviceResponse.builder()
                                                .deviceId(1L)
                                                .deviceName("Device 1")
                                                .build();

        when(deviceService.createEntities(anyList())).thenReturn(List.of(response));

        mockMvc.perform(post("/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }



    @Test
    void getDevicesByIdsAndTimeRangeReturns200() throws Exception {
        List<DeviceResponse> responses = List.of(
            DeviceResponse.builder().deviceId(1L).deviceName("Device 1").build(),
            DeviceResponse.builder().deviceId(2L).deviceName("Device 2").build()
        );

        when(deviceService.findByIdsAndTimeRange(List.of(1L, 2L), 1000L, 2000L))
                .thenReturn(responses);

        mockMvc.perform(get("/devices/logs/1,2")
                .param("from", "1000")
                .param("to", "2000"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responses)));
    }



    @Test
    void getDevicesByIdsAndTimeRangeWithFieldsSelectionReturns200() throws Exception {
        List<Map<String, Object>> responses = List.of(
            Map.of("id", 1L, "temp", 25),
            Map.of("id", 2L, "temp", 30)
        );

        when(deviceService.findByIdsAndTimeRangeWithFieldsSelection(List.of(1L, 2L), 1000L, 2000L, List.of("temp")))
                .thenReturn(responses);

        mockMvc.perform(get("/devices/logs/fields/1,2")
                .param("from", "1000")
                .param("to", "2000")
                .param("fields", "temp"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responses)));
    }

    
}