package com.plutus360.chronologix.tests.mappers;


import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.plutus360.chronologix.dtos.IntegrationTokenResponse;
import com.plutus360.chronologix.dtos.IntegrationTokenWithRaw;
import com.plutus360.chronologix.entities.IntegrationToken;
import com.plutus360.chronologix.mapper.IntegrationTokenMapper;
import com.trackswiftly.utils.base.services.ACLManager;
import com.trackswiftly.utils.base.services.CompressedAclService;
import com.trackswiftly.utils.dtos.TokenInfo;

import static org.assertj.core.api.Assertions.assertThat;


class IntegrationTokenMapperTest {

    private IntegrationTokenMapper mapper;



    @BeforeEach
    void setUp() {
        mapper = new IntegrationTokenMapper();
    }


    @Test
    void shouldMapIntegrationTokenToResponseCorrectly() {
        // given
        IntegrationToken token = IntegrationToken.builder()
                .id(1L)
                .name("TestToken")
                .active(true)
                .tokenIndexed("compressed-acl")
                .userId("user-123")
                .tokenHash("hash-abc")
                .expiredAt(OffsetDateTime.now().plusDays(1))
                .createdAt(OffsetDateTime.now())
                .build();

        // when
        IntegrationTokenResponse response = mapper.toIntegrationTokenResponse(token);

        // then
        assertThat(response.getName()).isEqualTo(token.getName());
        assertThat(response.getActive()).isEqualTo(token.getActive());
        assertThat(response.getCreatedAt()).isEqualTo(token.getCreatedAt());
        assertThat(response.getTokenInfo())
                .as("tokenInfo should be decompressed correctly")
                .isEqualTo(CompressedAclService.decompressAcl(token.getTokenIndexed()));
    }



    @Test
    void shouldMapRequestToEntityWithRawTokenCorrectly() {
        // given
        TokenInfo tokenInfo = new TokenInfo(); // build a proper TokenInfo depending on your domain
        IntegrationTokenRequest request = new IntegrationTokenRequest();
        request.setName("RequestToken");
        request.setActive(true);
        request.setTokenInfo(tokenInfo);
        request.setExpiredAt(OffsetDateTime.now().plusDays(5));

        // when
        IntegrationTokenWithRaw result = mapper.toIntegrationToken(request);

        // then
        assertThat(result.rawToken()).isNotBlank();
        assertThat(result.rawToken()).hasSize(64);

        IntegrationToken entity = result.entity();
        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getActive()).isEqualTo(request.getActive());
        assertThat(entity.getExpiredAt()).isEqualTo(request.getExpiredAt());
        assertThat(entity.getUserId()).isEqualTo("uu-sh-hello-testo"); // fixed value
        assertThat(entity.getTokenHash()).isNotBlank();
        assertThat(entity.getTokenIndexed())
                .isEqualTo(CompressedAclService.compressAcl(new ACLManager().getAclTable(tokenInfo)));
    }



    @Test
    void shouldMapListOfTokensToResponseListCorrectly() {
        // given
        IntegrationToken token1 = IntegrationToken.builder()
                .name("Token1")
                .active(true)
                .tokenIndexed("compressed-1")
                .createdAt(OffsetDateTime.now())
                .build();

        IntegrationToken token2 = IntegrationToken.builder()
                .name("Token2")
                .active(false)
                .tokenIndexed("compressed-2")
                .createdAt(OffsetDateTime.now())
                .build();

        List<IntegrationToken> tokens = List.of(token1, token2);

        // when
        List<IntegrationTokenResponse> responses = mapper.toIntegrationTokenResponseList(tokens);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Token1");
        assertThat(responses.get(1).getName()).isEqualTo("Token2");
    }



    @Test
    void shouldMapListOfRequestsToEntityListCorrectly() {
        // given
        IntegrationTokenRequest request1 = new IntegrationTokenRequest();
        request1.setName("Req1");
        request1.setActive(true);
        request1.setTokenInfo(new TokenInfo());
        request1.setExpiredAt(OffsetDateTime.now().plusDays(2));

        IntegrationTokenRequest request2 = new IntegrationTokenRequest();
        request2.setName("Req2");
        request2.setActive(false);
        request2.setTokenInfo(new TokenInfo());
        request2.setExpiredAt(OffsetDateTime.now().plusDays(3));

        List<IntegrationTokenRequest> requests = List.of(request1, request2);

        // when
        List<IntegrationTokenWithRaw> entities = mapper.toIntegrationTokenList(requests);

        // then
        assertThat(entities).hasSize(2);

        assertThat(entities.get(0).entity().getName()).isEqualTo("Req1");
        assertThat(entities.get(1).entity().getName()).isEqualTo("Req2");
    }
}
