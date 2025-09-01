package com.plutus360.chronologix.tests.dtos;


import java.time.OffsetDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.plutus360.chronologix.dtos.DeviceRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;


class DeviceRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator  = factory.getValidator();
    }



    @Test
    void shouldFailWhenDeviceNameBlank() {
        DeviceRequest request = DeviceRequest.builder()
                .deviceName(" ")
                .ident("abc123")
                .deviceId(10L)
                .serverTimestamp(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<DeviceRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Device name is required"));
    }




    @Test
    void shouldFailWhenDeviceIdNegative() {
        DeviceRequest request = DeviceRequest.builder()
                .deviceName("My Device")
                .ident("abc123")
                .deviceId(-5L)
                .serverTimestamp(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<DeviceRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Device id must be positive"));
    }



    @Test
    void shouldFailWhenServerTimestampInFuture() {
        DeviceRequest request = DeviceRequest.builder()
                .deviceName("My Device")
                .ident("abc123")
                .deviceId(1L)
                .serverTimestamp(OffsetDateTime.now().plusDays(1)) // invalid
                .build();

        Set<ConstraintViolation<DeviceRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Server timestamp must be in the present or past"));
    }




    @Test
    void shouldPassWhenAllFieldsValid() {
        DeviceRequest request = DeviceRequest.builder()
                .deviceName("Valid Device")
                .ident("abc123")
                .deviceId(100L)
                .serverTimestamp(OffsetDateTime.now().minusHours(1))
                .build();

        Set<ConstraintViolation<DeviceRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
    
}
