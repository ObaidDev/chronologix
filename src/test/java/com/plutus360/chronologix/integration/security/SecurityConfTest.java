package com.plutus360.chronologix.integration.security;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.plutus360.chronologix.filters.JwtAuthenticationFilter;
import com.plutus360.chronologix.security.CorsConfig;
import com.plutus360.chronologix.security.SecurityConf;
import com.plutus360.chronologix.service.AuthenticationService;
import com.plutus360.chronologix.service.JwtService;
import com.plutus360.chronologix.utils.CustomACLManager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// class SecurityConfTest {
//     @Autowired
//     private MockMvc mockMvc;
    
//     @Test
//     void shouldAllowAccessToWhitelistedEndpoints() throws Exception {
//         mockMvc.perform(get("/swagger-ui/index.html"))
//                .andExpect(status().isOk());
//     }
// }


