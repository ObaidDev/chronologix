package com.plutus360.chronologix.filters;

import java.io.IOException;
import java.util.List;


import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plutus360.chronologix.dtos.ErrorResponse;
import com.plutus360.chronologix.exception.InvalideTokenException;
import com.plutus360.chronologix.service.IntegrationTokenService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Builder
@Slf4j
public class PlutusTokenFilter implements Filter{


    private final String targetEndpoint;

    private final IntegrationTokenService integrationTokenService;




    

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestPath = httpRequest.getServletPath();
        String httpMethod = httpRequest.getMethod();

        if (requestPath.contains(targetEndpoint)) {
            // Extract the PlutusAuthorization header
            String plutusToken = httpRequest.getHeader("PlutusAuthorization");
            
            if (plutusToken != null && !plutusToken.isEmpty()) {

                log.info("Path : {} 🐤", requestPath);
                log.info("Method : {} 🚀", httpMethod);


                try {
                    integrationTokenService.checkAcess(plutusToken, "gw" + requestPath, httpMethod);
                } catch (RuntimeException ex) {
                    handleUnableToProccessIteamException(ex, response);
                    return;
                }

                
                
            }

            else {
                
                throw new InvalideTokenException("Missing PlutusAuthorization header");
            }
        }
        

        chain.doFilter(request, response);
    }





    private void handleUnableToProccessIteamException(RuntimeException ex , ServletResponse response) throws StreamWriteException, DatabindException, IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN); // Or other status
        httpResponse.setContentType("application/json");
        
        // Write custom error response to the response body
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(false);
        errorResponse.setMessage("Token validation failed");
        errorResponse.setErrors(List.of(new ErrorResponse.ErrorDetail("general", ex.getMessage())));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(httpResponse.getWriter(), errorResponse);
    
    }
}
