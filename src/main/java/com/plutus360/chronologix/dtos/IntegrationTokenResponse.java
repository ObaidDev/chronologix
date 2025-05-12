package com.plutus360.chronologix.dtos;



import com.fasterxml.jackson.annotation.JsonProperty;
import com.trackswiftly.utils.dtos.TokenInfo;

import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationTokenResponse {


    @JsonProperty("token.id")
    private Long id;

    @JsonProperty("user.id")
    private Long userId;


    @JsonProperty("name")
    private String name;

    // Use TokenInfo custom type for the token info details
    @JsonProperty("token.info")
    private TokenInfo tokenInfo;

    @JsonProperty("active")
    private Boolean active;


    @JsonProperty("created.at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated.at")
    private OffsetDateTime updatedAt;
    
}
