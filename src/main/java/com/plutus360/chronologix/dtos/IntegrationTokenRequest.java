package com.plutus360.chronologix.dtos;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.plutus360.chronologix.types.TokenInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationTokenRequest {

    @NotNull(message = "Token name is required")
    @Size(min = 3, max = 50, message = "Name must be 3-50 characters")
    @JsonProperty("token.name")
    private String name;


    @Valid
    @NotNull(message = "Token info is required")
    @JsonProperty("token.info")
    private TokenInfo tokenInfo;

    
    @NotNull(message = "Active status is required")
    @JsonProperty("token.active")
    private Boolean active;


}
