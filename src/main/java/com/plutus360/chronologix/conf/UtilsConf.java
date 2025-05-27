package com.plutus360.chronologix.conf;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class UtilsConf {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /***
     * 
     * swagger config
     */


    @Bean
    public OpenAPI customOpenAPI() {
    
        String serverUrl = contextPath.equals("/") ? "" : contextPath;

        return new OpenAPI()
        .addServersItem(new Server().url(serverUrl).description("Server URL"))
        .info(new Info()
                .title("TMS Client Service")
                .description("Multi-tenant client service for the TMS Platform")
                .version("v1.0.0")
        )
        // Add other OpenAPI configurations (security, components, etc.)
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(new Components()
                .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
        );
    }
}
