package com.plutus360.chronologix.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.plutus360.chronologix.entities.User;
import com.plutus360.chronologix.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserCreationRunner implements CommandLineRunner {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    

    @Autowired
    public UserCreationRunner (UserService userService , BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.create:false}")
    private boolean createAdmin;

    @Override
    public void run(String... args) throws Exception {

        // Check for command line arguments
        if (args.length > 0 && "create-user".equals(args[0])) {
            handleUserCreationFromArgs(args);
        }
    }

    private void handleUserCreationFromArgs(String[] args) {
        if (args.length < 4) {
            log.error("Usage: java -jar app.jar create-user <username> <email> <password>");
            return;
        }

        String username = args[1];
        String email = args[2];
        String password = args[3];

        try {
            // Check if user already exists
            User existingUser = userService.getUserByUsername(username);
            if (existingUser != null) {
                log.error("❌ User '{}' already exists", username);
                return;
            }

            // Create new user
            User newUser = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

            User createdUser = userService.createUser(newUser);
            log.info("✅ User created successfully: {}", createdUser.getUsername());

        } catch (Exception e) {
            log.error("❌ Failed to create user", e);
        }
    }
}