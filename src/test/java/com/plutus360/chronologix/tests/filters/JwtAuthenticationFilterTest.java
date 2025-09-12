package com.plutus360.chronologix.tests.filters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.plutus360.chronologix.filters.JwtAuthenticationFilter;
import com.plutus360.chronologix.service.JwtService;
import com.plutus360.chronologix.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for JwtAuthenticationFilter
 * Tests JWT token validation and authentication flow
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
@DisabledInAotMode
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService mockJwtService;

    @Mock
    private UserService mockUserDetailsService;

    @Mock
    private HandlerExceptionResolver mockHandlerExceptionResolver;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private FilterChain mockFilterChain;

    @Mock
    private UserDetails mockUserDetails;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(
            mockJwtService, 
            mockUserDetailsService, 
            mockHandlerExceptionResolver
        );
        
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate user when valid JWT token provided")
    void should_AuthenticateUser_When_ValidJwtTokenProvided() throws IOException, ServletException {
        // Arrange
        String validToken = "valid.jwt.token";
        String bearerToken = "Bearer " + validToken;
        String userEmail = "test@example.com";
        
        when(mockRequest.getHeader("Authorization")).thenReturn(bearerToken);
        when(mockJwtService.extractUsername(validToken)).thenReturn(userEmail);
        when(mockJwtService.isTokenValid(eq(validToken), any(UserDetails.class))).thenReturn(true);
        when(mockUserDetailsService.loadUserByUsername(userEmail)).thenReturn(mockUserDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockJwtService, times(1)).extractUsername(validToken);
        verify(mockUserDetailsService, times(1)).loadUserByUsername(userEmail);
        verify(mockJwtService, times(1)).isTokenValid(validToken, mockUserDetails);
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockHandlerExceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should skip authentication when no Authorization header present")
    void should_SkipAuthentication_When_NoAuthorizationHeaderPresent() throws IOException, ServletException {
        // Arrange
        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockJwtService, never()).extractUsername(anyString());
        verify(mockUserDetailsService, never()).loadUserByUsername(anyString());
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("Should skip authentication when Authorization header does not start with Bearer")
    void should_SkipAuthentication_When_AuthorizationHeaderNotBearer() throws IOException, ServletException {
        // Arrange
        when(mockRequest.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // Act
        jwtAuthenticationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockJwtService, never()).extractUsername(anyString());
        verify(mockJwtService, never()).isTokenValid(null, mockUserDetails);
        verify(mockUserDetailsService, never()).loadUserByUsername(anyString());
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockHandlerExceptionResolver, never()).resolveException(any(), any(), any(), any());
    }


    @Test
    @DisplayName("Should skip authentication when user already authenticated")
    void should_SkipAuthentication_When_UserAlreadyAuthenticated() throws IOException, ServletException {
        // Arrange
        String validToken = "valid.jwt.token";
        String bearerToken = "Bearer " + validToken;
        String userEmail = "test@example.com";
        
        // Mock existing authentication in SecurityContext
        UsernamePasswordAuthenticationToken existingAuth = 
            new UsernamePasswordAuthenticationToken(userEmail, null, createAuthorities("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        when(mockRequest.getHeader("Authorization")).thenReturn(bearerToken);
        when(mockJwtService.extractUsername(validToken)).thenReturn(userEmail);

        // Act
        jwtAuthenticationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockJwtService, times(1)).extractUsername(validToken);
        verify(mockUserDetailsService, never()).loadUserByUsername(anyString());
        verify(mockJwtService, never()).isTokenValid(anyString(), any(UserDetails.class));
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockHandlerExceptionResolver, never()).resolveException(any(), any(), any(), any());
    }


    /**
     * Helper method to create authorities for testing
     */
    private Collection<? extends GrantedAuthority> createAuthorities(String... roles) {
        return Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}