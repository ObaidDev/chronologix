package com.plutus360.chronologix.dtos;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRequest {


    @JsonProperty("device.name")
    private String deviceName;

    @JsonProperty("ident")
    private String ident;

    @JsonProperty("device.id")
    private Long deviceId;

    @JsonProperty("server.timestamp")
    private OffsetDateTime serverTimestamp;

    // Store all other dynamic fields here
    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();



    @JsonAnySetter
    public void setDynamicField(String name, Object value) {
        if (!"device.name".equals(name) &&
            !"ident".equals(name) &&
            !"device.id".equals(name) &&
            !"server.timestamp".equals(name)) {
            payload.put(name, value);
        }
    }
    
}
