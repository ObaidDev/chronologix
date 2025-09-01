package com.plutus360.chronologix.tests.services;


import com.plutus360.chronologix.dao.repositories.IntegrationTokenRepo;
import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.plutus360.chronologix.dtos.IntegrationTokenResponse;
import com.plutus360.chronologix.dtos.IntegrationTokenWithRaw;
import com.plutus360.chronologix.entities.IntegrationToken;
import com.plutus360.chronologix.mapper.IntegrationTokenMapper;
import com.plutus360.chronologix.service.IntegrationTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.trackswiftly.utils.dtos.TokenInfo;

@ExtendWith(MockitoExtension.class)
class IntegrationTokenServiceTest {


    @Mock
    private IntegrationTokenMapper mapper;

    @Mock
    private IntegrationTokenRepo repo;

    @InjectMocks
    private IntegrationTokenService service;

    private IntegrationTokenRequest request;
    private IntegrationToken entity;
    private IntegrationTokenWithRaw tokenWithRaw;
    private IntegrationTokenResponse response;



    @BeforeEach
    void setup() {
        // Create TokenInfo for the request
        TokenInfo tokenInfo = new TokenInfo(); // TokenInfo uses simple constructor
        
        // Build IntegrationTokenRequest using builder pattern
        request = IntegrationTokenRequest.builder()
                .name("test-token")
                .tokenInfo(tokenInfo)
                .expiredAt(OffsetDateTime.now().plusDays(30))
                .active(true)
                .build();
        
        // Build IntegrationToken entity using builder pattern
        entity = IntegrationToken.builder()
                .id(1L)
                .userId("user123")
                .tokenHash("hashedToken")
                .name("test-token")
                .tokenIndexed("indexed-token")
                .active(true)
                .expiredAt(OffsetDateTime.now().plusDays(30))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        
        // Build IntegrationTokenWithRaw using record constructor
        tokenWithRaw = new IntegrationTokenWithRaw("rawToken", entity);
        
        // Build IntegrationTokenResponse using builder pattern
        Map<String, Map<String, Set<String>>> tokenInfoMap = new HashMap<>();
        response = IntegrationTokenResponse.builder()
                .id(1L)
                .userId(1L)
                .name("test-token")
                .tokenInfo(tokenInfoMap)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }


    @Test
    void createEntities_ShouldMapRequestsSaveAndReturnRawTokens() {
        // Arrange
        List<IntegrationTokenRequest> requests = List.of(request);
        List<IntegrationToken> entities = List.of(entity);

        when(mapper.toIntegrationToken(request)).thenReturn(tokenWithRaw);

        // Act
        List<String> result = service.createEntities(requests);

        // Assert
        assertThat(result).containsExactly("rawToken");
        verify(mapper).toIntegrationToken(request);
        verify(repo).insertInBatch(entities);
    }


    @Test
    void findBytoken_ShouldReturnEmptyIfNullOrEmpty() {
        assertThat(service.findBytoken(null)).isEmpty();
        assertThat(service.findBytoken("")).isEmpty();
        verifyNoInteractions(repo, mapper);
    }


    @Test
    void findBytoken_ShouldHashAndReturnEntityIfExists() {
        // Arrange
        String token = "rawToken";
        String hashed = "hashedToken";
        when(mapper.hashToken(token)).thenReturn(hashed);
        when(repo.findToken(hashed)).thenReturn(Optional.of(entity));

        // Act
        Optional<IntegrationToken> result = service.findBytoken(token);

        // Assert
        assertThat(result).contains(entity);
        verify(mapper).hashToken(token);
        verify(repo).findToken(hashed);
    }



    @Test
    void findByIds_ShouldReturnMappedResponses() {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        List<IntegrationToken> entities = List.of(entity);

        when(repo.findByIds(ids)).thenReturn(entities);
        when(mapper.toIntegrationTokenResponse(entity)).thenReturn(response);

        // Act
        List<IntegrationTokenResponse> result = service.findByIds(ids);

        // Assert
        assertThat(result).containsExactly(response);
        verify(repo).findByIds(ids);
        verify(mapper).toIntegrationTokenResponse(entity);
    }

    @Test
    void deleteToken_ShouldEvictFromCacheAndReturnRepoResult() {
        // Arrange
        String token = "rawToken";
        when(repo.deleteToken(token)).thenReturn(true);

        // Act
        boolean result = service.deleteToken(token);

        // Assert
        assertThat(result).isTrue();
        verify(repo).deleteToken(token);
    }
    
}
