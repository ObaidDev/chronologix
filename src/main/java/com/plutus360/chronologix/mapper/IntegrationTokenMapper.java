package com.plutus360.chronologix.mapper;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Component;


import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.plutus360.chronologix.dtos.IntegrationTokenResponse;
import com.plutus360.chronologix.entities.IntegrationToken;
import com.plutus360.chronologix.types.TokenInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IntegrationTokenMapper {


    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom secureRandom = new SecureRandom(); // Reuse SecureRandom




    public List<IntegrationTokenResponse> toIntegrationTokenResponseList(List<IntegrationToken> integrationTokens) {

        log.debug("IntegrationToken :{} " , integrationTokens);

        List<IntegrationTokenResponse> integrationTokenResponses = integrationTokens.stream()
                                            .map(this::toIntegrationTokenResponse)
                                            .toList() ;
        
        log.debug("group responses :{} " , integrationTokenResponses);
        return integrationTokenResponses;
    }



    public List<IntegrationToken> toIntegrationTokenList(List<IntegrationTokenRequest> integrationTokenRequests) {
        return integrationTokenRequests.stream()
                .map(this::toIntegrationToken)
                .toList();
    }






    public IntegrationTokenResponse toIntegrationTokenResponse(IntegrationToken token) {
        return IntegrationTokenResponse.builder()
                .name(token.getName())
                .active(token.getActive())
                .tokenInfo((TokenInfo) token.getTokenInfo())
                .createdAt(token.getCreatedAt())
                .build();
    }



    public IntegrationToken toIntegrationToken(IntegrationTokenRequest integrationTokenRequest) {
        return IntegrationToken.builder()
                .name(integrationTokenRequest.getName())
                .active(integrationTokenRequest.getActive())
                .tokenInfo(integrationTokenRequest.getTokenInfo())
                .userId("uu-sh-hello-testo")
                .tokenHash(
                    this.generateTokenHash(integrationTokenRequest)
                )
                .build();
    }





    private String generateTokenHash(IntegrationTokenRequest integrationTokenRequest) {

        StringBuilder tokenBuilder = new StringBuilder(64);
        
        tokenBuilder.append(integrationTokenRequest.getName().replaceAll("[^a-zA-Z0-9]", ""));
        
        tokenBuilder.append(Long.toHexString(Instant.now().toEpochMilli()));
        
        // Append 32 random alphanumeric chars
        for (int i = 0; i < 64 ; i++) {
            tokenBuilder.append(ALPHANUMERIC.charAt(secureRandom.nextInt(ALPHANUMERIC.length())));
        }
        
        return tokenBuilder.toString();
    }
    
}
