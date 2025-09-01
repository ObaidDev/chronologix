package com.plutus360.chronologix.tests.services;




import com.plutus360.chronologix.dao.repositories.UserRepo;
import com.plutus360.chronologix.dtos.LoginUserDto;
import com.plutus360.chronologix.dtos.RegisterUserDto;
import com.plutus360.chronologix.entities.User;
import com.plutus360.chronologix.service.AuthenticationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {



    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authService;

    private RegisterUserDto registerDto;
    private LoginUserDto loginDto;
    private User user;



    @BeforeEach
    void setup() {
        registerDto = new RegisterUserDto("john", "john@example.com", "password123");
        loginDto = new LoginUserDto("john@example.com", "password123");

        user = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .password("encodedPassword")
                .build();
    }





    @Test
    void signup_ShouldEncodePasswordAndSaveUser() {
        // Arrange
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("encodedPassword");
        when(userRepo.insert(any(User.class))).thenReturn(user);

        // Act
        User result = authService.signup(registerDto);

        // Assert
        assertThat(result).isEqualTo(user);
        verify(passwordEncoder).encode(registerDto.getPassword());
        verify(userRepo).insert(any(User.class));
    }

    @Test
    void authenticate_ShouldAuthenticateAndReturnUser() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(user));

        // Act
        User result = authService.authenticate(loginDto);

        // Assert
        assertThat(result).isEqualTo(user);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepo).findByEmail(loginDto.getEmail());
    }

    
    @Test
    void authenticate_ShouldThrowIfUserNotFound() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                java.util.NoSuchElementException.class,
                () -> authService.authenticate(loginDto)
        );
    }
    
}
