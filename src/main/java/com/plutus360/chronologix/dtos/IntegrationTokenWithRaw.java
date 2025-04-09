package com.plutus360.chronologix.dtos;

import com.plutus360.chronologix.entities.IntegrationToken;

public record IntegrationTokenWithRaw(String rawToken, IntegrationToken entity) {}
