package com.plutus360.chronologix.tests.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.plutus360.chronologix.dtos.IntegrationTokenResponse;
import com.plutus360.chronologix.service.IntegrationTokenService;
import com.plutus360.chronologix.web.IntegrationTokenController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



class IntegrationTokenControllerTest {


    private MockMvc mockMvc;

    @Mock
    private IntegrationTokenService integrationTokenService;

    @InjectMocks
    private IntegrationTokenController integrationTokenController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(integrationTokenController).build();
    }




    @Test
    void createTokensValidRequestReturns200() throws Exception {
        IntegrationTokenRequest request = IntegrationTokenRequest.builder()
                .name("Test Integration")
                .build();

        List<String> tokens = List.of("token-1", "token-2");

        when(integrationTokenService.createEntities(anyList())).thenReturn(tokens);

        mockMvc.perform(post("/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(tokens)));
    }
    


    @Test
    void findByIdsReturns200() throws Exception {
        List<IntegrationTokenResponse> responses = List.of(
            IntegrationTokenResponse.builder().id(1L).name("Token A").active(true).build(),
            IntegrationTokenResponse.builder().id(2L).name("Token B").active(false).build()
        );

        when(integrationTokenService.findByIds(List.of(1L, 2L))).thenReturn(responses);

        mockMvc.perform(get("/tokens/1,2"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responses)));
    }
}
