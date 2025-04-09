package com.plutus360.chronologix.security;



import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.plutus360.chronologix.filters.PlutusTokenFilter;
import com.plutus360.chronologix.service.IntegrationTokenService;

import java.security.KeyFactory;
import java.util.Base64;

import org.springframework.core.io.ClassPathResource;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConf {


    private final CorsConfigurationSource corsConfigurationSource ;

    private final IntegrationTokenService integrationTokenService;




    private static final String[] AUTH_WHITELIST = {
        // -- Swagger UI v2
        "/v2/api-docs",
        "/swagger-resources",
        "/swagger-resources/**",
        "/configuration/ui",
        "/swagger-ui.html",
        // -- Swagger UI v3 (OpenAPI) 
        "/v3/api-docs/**",
        "/swagger-ui/**"  ,

        "/actuator/**" ,

        "/tokens/**" ,
        "/devices/**" ,

    };


    @Autowired
    SecurityConf(
        CorsConfigurationSource corsConfigurationSource,
        IntegrationTokenService integrationTokenService) { 
        this.corsConfigurationSource = corsConfigurationSource;
        this.integrationTokenService = integrationTokenService; 
    }



    @Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http ) throws Exception {


        http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests( authorize  ->  {
                authorize.requestMatchers(AUTH_WHITELIST).permitAll() ;
                // authorize.anyRequest().authenticated() ;
            }
        )
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable);


        http.addFilterAfter(
            PlutusTokenFilter.builder()
                .targetEndpoint("devices")
                .integrationTokenService(integrationTokenService)
                .build(), 
            SecurityContextHolderFilter.class
        );

        return http.build() ;


    }
    
}
