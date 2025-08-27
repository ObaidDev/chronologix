package com.plutus360.chronologix.service;



import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.plutus360.chronologix.dao.repositories.IntegrationTokenRepo;
import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.plutus360.chronologix.dtos.IntegrationTokenResponse;
import com.plutus360.chronologix.dtos.IntegrationTokenWithRaw;
import com.plutus360.chronologix.entities.IntegrationToken;
import com.plutus360.chronologix.mapper.IntegrationTokenMapper;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class IntegrationTokenService {


    private IntegrationTokenMapper integrationTokenMapper;

    private IntegrationTokenRepo integrationTokenRepo;




    @Autowired
    IntegrationTokenService(
        IntegrationTokenMapper integrationTokenMapper,
        IntegrationTokenRepo integrationTokenRepo 
    ) {
        this.integrationTokenMapper = integrationTokenMapper;
        this.integrationTokenRepo = integrationTokenRepo;
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


    @Cacheable(value = "integrationTokens"  , key = "#token")
    public Optional<IntegrationToken> findBytoken(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }


        return integrationTokenRepo.findToken(integrationTokenMapper.hashToken(token));
    }



    public List<IntegrationTokenResponse> findByIds(List<Long> ids) {
        return integrationTokenRepo.findByIds(ids)
            .stream()
            .map(integrationTokenMapper::toIntegrationTokenResponse)
            .toList();
    }


    @CacheEvict(value = "integrationTokens", key = "#token") // delete the token from cache 
    public boolean deleteToken(String token) {
       return integrationTokenRepo.deleteToken(token);
    }


    
}
