package com.plutus360.chronologix.service;



import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.plutus360.chronologix.dao.repositories.UserRepo;
import com.plutus360.chronologix.dtos.LoginUserDto;
import com.plutus360.chronologix.dtos.RegisterUserDto;
import com.plutus360.chronologix.entities.User;
import com.plutus360.chronologix.exception.UnableToProccessIteamException;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
// @Transactional
public class AuthenticationService {

    private final UserRepo userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
        UserRepo userRepository,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User signup(RegisterUserDto input) {

        if (userRepository.countUsers() > 0) {
            log.warn("Signup attempt blocked. A user already exists 🚫");
            throw new UnableToProccessIteamException("Signup is disabled. An admin user already exists.");
        }

        User user = User.builder()
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .username(input.getUsername())
                .build();

        return userRepository.insert(user);
    }


    public User authenticate(LoginUserDto input) {

        log.info("Authenticating user with email: {} 🐛", input.getEmail());
        
        log.info("Password provided: {} ㊙️", input.getPassword());

        Authentication auth = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    input.getEmail(),
                                    input.getPassword()
                            )
                    );

        log.info("Authentication successful: {} 🔥", auth.isAuthenticated());

        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow() ;
        
        log.info("User authenticated successfully: {} 🌤️", user);

        return user;
    }
}