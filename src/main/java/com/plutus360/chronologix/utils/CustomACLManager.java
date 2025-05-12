package com.plutus360.chronologix.utils;




import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.plutus360.chronologix.entities.IntegrationToken;
import com.plutus360.chronologix.exception.UnableToProccessIteamException;
import com.plutus360.chronologix.service.IntegrationTokenService;
import com.trackswiftly.utils.base.services.CompressedAclService;

import lombok.extern.slf4j.Slf4j;



@Slf4j
@Component
public class CustomACLManager extends com.trackswiftly.utils.base.services.ACLManager{


    private IntegrationTokenService integrationTokenService;

    @Autowired
    CustomACLManager(IntegrationTokenService integrationTokenService) {
        this.integrationTokenService = integrationTokenService;
    }



    /**
     * utility method to check if the token is valid
     * @param token
     * @param uri
     * @param method
     */

    public void checkAcess(String token ,String uri , String method) {

        if (token == null || token.isEmpty()) {
            throw new UnableToProccessIteamException("Token cannot be null or empty");
        }

        IntegrationToken integrationToken = integrationTokenService.findBytoken(token)
        .orElseThrow(() -> new UnableToProccessIteamException("Token not found: " + token));

        // Check expiration
        if (integrationToken.getExpiredAt() != null && 
            integrationToken.getExpiredAt().isBefore(OffsetDateTime.now())) {
            throw new UnableToProccessIteamException("Token has expired");
        }

        // Map<String, Object> tokenInfo = integrationToken.getTokenInfo();


        // Check access permissions
        // if (!hasAccess(getAclTable(tokenInfo), uri, method, null)) {
        //     throw new UnableToProccessIteamException("Access denied for token " + token);
        // }

        Map<String,Map<String,Set<String>>> tokenInfo = CompressedAclService.decompressAcl(integrationToken.getTokenIndexed());

        log.info("🔑 Token info: {} ", tokenInfo);

        if (!hasAccess(tokenInfo, uri, method, null)) {
            throw new UnableToProccessIteamException("Access denied for token " + token);
        }

    }
    
}
