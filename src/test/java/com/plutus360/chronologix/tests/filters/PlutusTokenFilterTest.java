package com.plutus360.chronologix.tests.filters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.aot.DisabledInAotMode;

import com.plutus360.chronologix.exception.InvalideTokenException;
import com.plutus360.chronologix.filters.PlutusTokenFilter;
import com.plutus360.chronologix.utils.CustomACLManager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for PlutusTokenFilter using Mockito
 * Tests core filter functionality with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlutusTokenFilter Unit Tests")
@DisabledInAotMode
class PlutusTokenFilterTest {

    @Mock
    private CustomACLManager mockAclManager;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private FilterChain mockFilterChain;

    private PlutusTokenFilter plutusTokenFilter;
    
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        List<String> targetEndpoints = List.of("/devices", "/users", "/tokens");
        
        plutusTokenFilter = PlutusTokenFilter.builder()
            .targetEndpoints(targetEndpoints)
            .aclManager(mockAclManager)
            .build();

        // Setup PrintWriter for response writing (only when needed)
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    @DisplayName("Should call checkAccess and continue filter chain when valid token provided")
    void should_CallCheckAccessAndContinueChain_When_ValidTokenProvided() throws IOException, ServletException {
        // Arrange
        String testPath = "/devices/123";
        String testToken = "valid-token";
        
        when(mockRequest.getServletPath()).thenReturn(testPath);
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getHeader("PlutusAuthorization")).thenReturn(testToken);
        
        // Mock successful authorization
        doNothing().when(mockAclManager).checkAcess(eq(testToken), eq("gw/devices"), eq("GET"));

        // Act
        plutusTokenFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockAclManager, times(1)).checkAcess(testToken, "gw/devices", "GET");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockResponse, never()).setStatus(any(Integer.class));
    }

    @Test
    @DisplayName("Should return 401 when authorization header is missing")
    void should_Return401_When_AuthorizationHeaderMissing() throws IOException, ServletException {
        // Arrange
        when(mockRequest.getServletPath()).thenReturn("/devices");
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getHeader("PlutusAuthorization")).thenReturn(null);
        
        // Setup writer for error response
        when(mockResponse.getWriter()).thenReturn(printWriter);

        // Act
        plutusTokenFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockAclManager, never()).checkAcess(any(), any(), any());
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(mockResponse, times(1)).setContentType("application/json");
        verify(mockFilterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Should return 403 when ACL manager throws exception")
    void should_Return403_When_ACLManagerThrowsException() throws IOException, ServletException {
        // Arrange
        String testPath = "/users/456";
        String invalidToken = "invalid-token";
        
        when(mockRequest.getServletPath()).thenReturn(testPath);
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getHeader("PlutusAuthorization")).thenReturn(invalidToken);
        
        // Setup writer for error response
        when(mockResponse.getWriter()).thenReturn(printWriter);
        
        // Mock authorization failure
        doThrow(new InvalideTokenException("Invalid token")).when(mockAclManager)
            .checkAcess(eq(invalidToken), eq("gw/users"), eq("POST"));

        // Act
        plutusTokenFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockAclManager, times(1)).checkAcess(invalidToken, "gw/users", "POST");
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(mockResponse, times(1)).setContentType("application/json");
        verify(mockFilterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Should skip filtering for non-target endpoints")
    void should_SkipFiltering_When_NonTargetEndpoint() throws IOException, ServletException {
        // Arrange
        when(mockRequest.getServletPath()).thenReturn("/public/health");
        when(mockRequest.getMethod()).thenReturn("GET");

        // Act
        plutusTokenFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockAclManager, never()).checkAcess(any(), any(), any());
        verify(mockRequest, never()).getHeader("PlutusAuthorization");
        verify(mockResponse, never()).setStatus(any(Integer.class));
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }
}