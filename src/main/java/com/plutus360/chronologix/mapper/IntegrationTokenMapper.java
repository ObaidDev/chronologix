package com.plutus360.chronologix.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Component;


import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.plutus360.chronologix.dtos.IntegrationTokenResponse;
import com.plutus360.chronologix.dtos.IntegrationTokenWithRaw;
import com.plutus360.chronologix.entities.IntegrationToken;
import com.trackswiftly.utils.base.services.ACLManager;
import com.trackswiftly.utils.base.services.CompressedAclService;
import com.trackswiftly.utils.dtos.TokenInfo;

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



    public List<IntegrationTokenWithRaw> toIntegrationTokenList(List<IntegrationTokenRequest> integrationTokenRequests) {
        return integrationTokenRequests.stream()
                .map(this::toIntegrationToken)
                .toList();
    }






    public IntegrationTokenResponse toIntegrationTokenResponse(IntegrationToken token) {
        return IntegrationTokenResponse.builder()
                .name(token.getName())
                .active(token.getActive())
                .tokenInfo(CompressedAclService.decompressAcl(token.getTokenIndexed()))
                .createdAt(token.getCreatedAt())
                .build();
    }



    public IntegrationTokenWithRaw toIntegrationToken(IntegrationTokenRequest integrationTokenRequest) {

        String token = generateToken();
        TokenInfo tokenInfo = integrationTokenRequest.getTokenInfo();
        String compressedacl =  CompressedAclService.compressAcl((new ACLManager()).getAclTable(tokenInfo));

        IntegrationToken entity = IntegrationToken.builder()
            .name(integrationTokenRequest.getName())
            .active(integrationTokenRequest.getActive())
            // .tokenInfo((TokenInfo) (Map<? , ? >) CompressedAclService.decompressAcl(token.getTokenIndexed()))
            .tokenIndexed(compressedacl)
            .userId("uu-sh-hello-testo")
            .tokenHash(hashToken(token))
            .expiredAt(integrationTokenRequest.getExpiredAt())
            .build();

        return new IntegrationTokenWithRaw(token, entity);
    }






    private String generateToken() {
        StringBuilder tokenBuilder = new StringBuilder(64);
        
        // Append 32 random alphanumeric chars
        for (int i = 0; i < 64; i++) {
            tokenBuilder.append(ALPHANUMERIC.charAt(secureRandom.nextInt(ALPHANUMERIC.length())));
        }
        
        return tokenBuilder.toString();
    }




    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            
            // Convert the byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
    
}
