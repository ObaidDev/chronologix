package com.plutus360.chronologix.tests.dtos;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.plutus360.chronologix.dtos.IntegrationTokenRequest;
import com.trackswiftly.utils.dtos.TokenInfo;
import com.trackswiftly.utils.enums.Resource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntegrationTokenRequestValidationTest {
    

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }




    @Test
    void shouldFailWhenNameIsNull() {
        IntegrationTokenRequest request = IntegrationTokenRequest.builder()
                .name(null)
                .active(true)
                .tokenInfo(new TokenInfo())
                .expiredAt(OffsetDateTime.now().plusDays(1))
                .build();

        Set<ConstraintViolation<IntegrationTokenRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Token name is required"));
    }



    @Test
    void shouldFailWhenExpiredDateInPast() {
        IntegrationTokenRequest request = IntegrationTokenRequest.builder()
                .name("ValidName")
                .active(true)
                .tokenInfo(new TokenInfo())
                .expiredAt(OffsetDateTime.now().minusDays(1))
                .build();

        Set<ConstraintViolation<IntegrationTokenRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Expired date must be in the present or future"));
    }



    @Test
    void shouldPassWhenAllFieldsValid() {
        IntegrationTokenRequest request = IntegrationTokenRequest.builder()
                .name("ValidName")
                .active(true)
                .tokenInfo(new TokenInfo())
                .expiredAt(OffsetDateTime.now().plusDays(1))
                .build();

        Set<ConstraintViolation<IntegrationTokenRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }




    @Test
    void shouldThrowExceptionWhenInvalidHttpMethodAddedToTokenInfo() {
        TokenInfo tokenInfo = new TokenInfo();
        Map<String, Object> value = Map.of(
                "methods", List.of("GET" , "INVALID_METHOD")
        );

        // act + assert
        assertThatThrownBy(() -> tokenInfo.put(Resource.DEVICES.getPath(), value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid HTTP method");
    }

}
