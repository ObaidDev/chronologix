package com.plutus360.chronologix.web;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.plutus360.chronologix.service.IntegrationTokenService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/tokens")
@Slf4j
@Validated
public class IntegrationTokenController {

    private IntegrationTokenService integrationTokenService;

    @Autowired
    IntegrationTokenController(
        IntegrationTokenService integrationTokenService
    ) {
        this.integrationTokenService = integrationTokenService;
    }


    @PostMapping
    public List<String> createTokens(
        @RequestBody @Valid List<IntegrationTokenRequest> integrationTokenRequests
    ) {

        return integrationTokenService.createEntities(integrationTokenRequests);

    }
    
}
