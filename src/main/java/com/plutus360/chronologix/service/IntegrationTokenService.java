package com.plutus360.chronologix.service;



import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plutus360.chronologix.dao.repositories.IntegrationTokenRepo;
import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.plutus360.chronologix.dtos.DeviceResponse;
import com.plutus360.chronologix.dtos.IntegrationTokenResponse;
import com.plutus360.chronologix.entities.Device;
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



    public List<IntegrationTokenResponse> createEntities(List<IntegrationTokenRequest> integrationTokenRequests) {


        log.info("Creating integration tokens in batch...");
        log.debug("integrationTokenRequests : {}" , integrationTokenRequests);
        
        return integrationTokenMapper.toIntegrationTokenResponseList(
                                    integrationTokenRepo.insertInBatch(
                                                        integrationTokenMapper.toIntegrationTokenList(
                                                            integrationTokenRequests
                                                        )
                                                    )
                                                )  ;
        
    }
    
}
