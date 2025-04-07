package com.plutus360.chronologix.dtos;



import java.time.OffsetDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponse {


    private String deviceName;
    private String ident;
    private Long deviceId;
    private OffsetDateTime serverTimestamp;
    private Map<String, Object> payload;
    
}
