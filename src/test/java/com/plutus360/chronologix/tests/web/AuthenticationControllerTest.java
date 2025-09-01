package com.plutus360.chronologix.tests.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plutus360.chronologix.dtos.LoginResponse;
import com.plutus360.chronologix.dtos.LoginUserDto;
import com.plutus360.chronologix.entities.User;
import com.plutus360.chronologix.service.AuthenticationService;
import com.plutus360.chronologix.service.JwtService;
import com.plutus360.chronologix.web.AuthenticationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class AuthControllerTest {


    private MockMvc mockMvc;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
    }



    @Test
    void loginValidCredentialsReturnsJwtToken() throws Exception {
        LoginUserDto loginUserDto = new LoginUserDto("test@example.com", "password123");
        User fakeUser = User.builder().id(1L).email("test@example.com").password("hashed").build();

        when(authenticationService.authenticate(loginUserDto)).thenReturn(fakeUser);
        when(jwtService.generateToken(fakeUser)).thenReturn("fake-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        LoginResponse expectedResponse = LoginResponse.builder()
                .token("fake-jwt-token")
                .expiresIn(3600L)
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

}
