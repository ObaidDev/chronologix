package com.plutus360.chronologix.service;



import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plutus360.chronologix.dao.repositories.IntegrationTokenRepo;
import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.plutus360.chronologix.dtos.IntegrationTokenWithRaw;
import com.plutus360.chronologix.entities.IntegrationToken;
import com.plutus360.chronologix.exception.UnableToProccessIteamException;
import com.plutus360.chronologix.mapper.IntegrationTokenMapper;
import com.plutus360.chronologix.utils.ACLManager;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class IntegrationTokenService {


    private IntegrationTokenMapper integrationTokenMapper;

    private IntegrationTokenRepo integrationTokenRepo;

    private final ACLManager aclManager;



    @Autowired
    IntegrationTokenService(
        IntegrationTokenMapper integrationTokenMapper,
        IntegrationTokenRepo integrationTokenRepo ,
        ACLManager aclManager
    ) {
        this.integrationTokenMapper = integrationTokenMapper;
        this.integrationTokenRepo = integrationTokenRepo;
        this.aclManager = aclManager;
    }



    public List<String> createEntities(List<IntegrationTokenRequest> integrationTokenRequests) {


        // Convert requests to token pairs
        List<IntegrationTokenWithRaw> tokenPairs = integrationTokenRequests.stream()
            .map(integrationTokenMapper::toIntegrationToken)
            .toList();

        // Extract just the entities for storage
        List<IntegrationToken> entities = tokenPairs.stream()
            .map(IntegrationTokenWithRaw::entity)  // Use method reference with record accessor
            .toList();

        // Store the entities with hashed tokens
        integrationTokenRepo.insertInBatch(entities);

        return tokenPairs.stream()
            .map(IntegrationTokenWithRaw::rawToken)
            .toList();


        
    }



    private Optional<IntegrationToken> findBytoken(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }


        return integrationTokenRepo.findToken(integrationTokenMapper.hashToken(token));
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

        IntegrationToken integrationToken = findBytoken(token)
        .orElseThrow(() -> new UnableToProccessIteamException("Token not found: " + token));

        // Check expiration
        if (integrationToken.getExpiredAt() != null && 
            integrationToken.getExpiredAt().isBefore(OffsetDateTime.now())) {
            throw new UnableToProccessIteamException("Token has expired");
        }

        Map<String, Object> tokenInfo = integrationToken.getTokenInfo();


        // Check access permissions
        if (!aclManager.hasAccess(getAclTable(tokenInfo), uri, method, null)) {
            throw new UnableToProccessIteamException("Access denied for token " + token);
        }

    }


    /**
     * utility method to check if the token is valid
     * @param token
     * @param uri
     * @param method
     */
    public Map<String, Map<String, Set<String>>> getAclTable (Map<String, Object> tokenInfo) {

        @SuppressWarnings("unchecked")
        Map<String, Map<String, List<String>>> rawAcl = (Map<String, Map<String, List<String>>>) (Map<?, ?>) tokenInfo;

        return aclManager.convertToSet(rawAcl);
    }
    

    





    
}
