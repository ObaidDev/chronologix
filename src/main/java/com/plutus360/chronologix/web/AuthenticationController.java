package com.plutus360.chronologix.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plutus360.chronologix.dtos.LoginResponse;
import com.plutus360.chronologix.dtos.LoginUserDto;
import com.plutus360.chronologix.dtos.RegisterUserDto;
import com.plutus360.chronologix.entities.User;
import com.plutus360.chronologix.service.AuthenticationService;
import com.plutus360.chronologix.service.JwtService;
import com.plutus360.chronologix.service.PasswordResetService;
import com.plutus360.chronologix.service.UserService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;
    private final UserService userService ;

    public AuthenticationController(
        JwtService jwtService, 
        AuthenticationService authenticationService ,
        PasswordResetService passwordResetService ,
        UserService userService
    ) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.passwordResetService = passwordResetService;
        this.userService = userService ;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {

        log.info("Authenticating user with email: {}", loginUserDto.getEmail());
        
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity.ok(loginResponse);
    }







    /*
     * 
     * Password fogeten
     */


    /**
     * Step 1: User requests password reset
     */
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
       
        return passwordResetService.forgotPassword(email);
    }



    /**
     * Step 2: User submits new password with token
     */
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                                @RequestParam String newPassword) {

        return passwordResetService.resetPassword(token, newPassword);
    }
}
