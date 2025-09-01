package com.plutus360.chronologix.integration.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.plutus360.chronologix.security.CorsConfig;


import static org.assertj.core.api.Assertions.assertThat;

// class CorsConfigTest {

//     private final CorsConfig corsConfig = new CorsConfig();

//     @Test
//     void shouldReturnCorsConfigurationSourceWithExpectedSettings() {
//         // when
//         CorsConfigurationSource source = corsConfig.corsConfigSource();

//         // then
//         assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);

//         CorsConfiguration corsConfiguration = source.getCorsConfiguration(null);
//         assertThat(corsConfiguration).isNotNull();

//         // verify values
//         assertThat(corsConfiguration.getAllowedOrigins()).containsExactly("*");
//         assertThat(corsConfiguration.getAllowedMethods())
//                 .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
//         assertThat(corsConfiguration.getAllowedHeaders())
//                 .containsExactlyInAnyOrder("Authorization", "Content-Type");
//     }
// }

