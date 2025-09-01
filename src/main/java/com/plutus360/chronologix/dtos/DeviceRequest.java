package com.plutus360.chronologix.dtos;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
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
    @NotBlank(message = "Device name is required")
    private String deviceName;

    @JsonProperty("ident")
    @NotBlank(message = "Device ident is required")
    private String ident;

    @JsonProperty("device.id")
    @NotNull(message = "Device id is required")
    @Positive(message = "Device id must be positive")
    private Long deviceId;

    @JsonProperty("server.timestamp")
    @PastOrPresent(message = "Server timestamp must be in the present or past")
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
