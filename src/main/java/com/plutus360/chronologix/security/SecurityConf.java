package com.plutus360.chronologix.security;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.plutus360.chronologix.filters.PlutusTokenFilter;
import com.plutus360.chronologix.utils.ACLManager;

import java.util.List;




@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConf {


    private final CorsConfigurationSource corsConfigurationSource ;

    private final ACLManager aclManager;




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
        ACLManager aclManager) { 
        this.corsConfigurationSource = corsConfigurationSource;
        this.aclManager = aclManager; 
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
                .targetEndpoints(
                    List.of(
                        "devices"
                    )
                )
                .aclManager(aclManager)
                .build(), 
            SecurityContextHolderFilter.class
        );

        return http.build() ;


    }
    
}
