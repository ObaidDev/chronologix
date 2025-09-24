package com.plutus360.chronologix.service;

import java.security.Key;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.plutus360.chronologix.dao.repositories.PasswordRepo;
import com.plutus360.chronologix.entities.PasswordResetToken;
import com.plutus360.chronologix.entities.User;
import com.plutus360.chronologix.exception.UnableToProccessIteamException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class PasswordResetService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${app.base-url}")
    private String baseUrl;


    private final PasswordRepo passwordRepo;
    private final UserService userService ;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService ;


    @Autowired
    public PasswordResetService(
        PasswordRepo passwordRepo ,
        UserService userService ,
        PasswordEncoder passwordEncoder ,
        EmailService emailService
    ) {
        this.passwordRepo = passwordRepo;
        this.userService = userService ;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService ;
    }



    public String forgotPassword (String email) 
    {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new UnableToProccessIteamException("User Not Found") ;
        }

        String token = generateResetToken(user.getId());

        String resetLink = baseUrl + "/auth/reset-password?token=" + token;

        emailService.sendSimpleMail(
            user.getEmail(),
            "Password Reset Request",
            "Click the link to reset your password: " + resetLink
        );

        return "none" ;
    }


    public String resetPassword (String token , String newPassword) {

        try {
            // Validate token and get userId
            Long userId = validateResetToken(token);

            // Update user password
            User user = userService.getUserById(userId);

            if (user == null) {
                throw new UnableToProccessIteamException("User Not Found") ;
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);

            // Mark token as used
            invalidateToken(token);

            return "password updated";

        } catch (Exception e) {
            return "Invalid or expired token.";
        }

    }

















     /**
     * Generate a password reset JWT and store it in DB
     */
    public String generateResetToken(Long userId) {
        String tokenId = String.valueOf(System.currentTimeMillis()) + Math.random(); // temporary unique string for JWT ID
        Instant expiresAt = Instant.now().plusSeconds(15 * 60); // 15 minutes

        String token = Jwts.builder()
                .setSubject(userId.toString())
                .claim("purpose", "reset_password")
                .setId(tokenId)
                .setExpiration(Date.from(expiresAt))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        // Save in DB


        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                                                                    .user(User.builder().id(userId).build())
                                                                    .token(token)
                                                                    .expiresAt(expiresAt)
                                                                    .createdAt(OffsetDateTime.now())
                                                                    .updatedAt(OffsetDateTime.now())
                                                                    .used(false)
                                                                    .build();

        passwordRepo.save(passwordResetToken);

        return token;
    }




    /**
     * Validate the reset token: signature, purpose, expiry, and if used
     */
    public Long validateResetToken(String token) {
        var claims = Jwts
                        .parserBuilder()
                        .setSigningKey(getSignInKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

        if (!"reset_password".equals(claims.get("purpose"))) {
            throw new IllegalArgumentException("Invalid token purpose");
        }

        var entity = passwordRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        if (entity.isUsed()) {
            throw new IllegalArgumentException("Token already used");
        }

        if (entity.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        return entity.getUser().getId();
    }

    /**
     * Mark token as used after password reset
     */
    public void invalidateToken(String token) {
        passwordRepo.findByToken(token).ifPresent(entity -> {
            entity.setUsed(true);
            passwordRepo.save(entity);
        });
    }












    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    
}
