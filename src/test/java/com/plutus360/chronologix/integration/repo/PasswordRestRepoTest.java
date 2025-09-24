package com.plutus360.chronologix.integration.repo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.plutus360.chronologix.dao.repositories.PasswordRepo;
import com.plutus360.chronologix.dao.repositories.UserRepo;
import com.plutus360.chronologix.entities.PasswordResetToken;
import com.plutus360.chronologix.entities.User;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PasswordRestRepoTest extends AbstractIntegrationTest {



    @Autowired
    private PasswordRepo passwordRepo;

    @Autowired
    private UserRepo userRepo ;

    private PasswordResetToken token1;
    private PasswordResetToken token2;

    private User testUser ;



    @BeforeEach
    void setUp() {
        // Create and save a user first (needed for foreign key constraint)
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashed_password")
                .build();

        // Save the user - adjust method name based on your UserRepo
        testUser = userRepo.insert(testUser); // or userRepo.insert(testUser) if that's your method
    }


    @Test
    void shouldSavePasswordResetToken() {
        // Given
        PasswordResetToken token = PasswordResetToken.builder()
                .user(testUser)
                .token("test-token-123")
                .expiresAt(Instant.now().plusSeconds(3600)) // 1 hour from now
                .used(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        // When
        PasswordResetToken savedToken = passwordRepo.save(token);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull(); // Should have generated ID
        assertThat(savedToken.getToken()).isEqualTo("test-token-123");
        assertThat(savedToken.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(savedToken.isUsed()).isFalse();
        assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());
    }


    @Test
    void shouldSaveTokenWithDefaultUsedValue() {
        // Given - using @Builder.Default for 'used' field
        PasswordResetToken token = PasswordResetToken.builder()
                .user(testUser)
                .token("test-token-456")
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                // not setting 'used' - should default to false
                .build();

        // When
        PasswordResetToken savedToken = passwordRepo.save(token);

        // Then
        assertThat(savedToken.isUsed()).isFalse();
    }


    @Test
    void shouldThrowExceptionWhenTokenTooLong() {
        // Given - test beyond the 512 character limit
        String tooLongToken = "a".repeat(513); // one char too many
        
        PasswordResetToken tokenTooLong = PasswordResetToken.builder()
                .user(testUser)
                .token(tooLongToken)
                .expiresAt(Instant.now().plusSeconds(3600))
                .used(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        // When/Then
        assertThatThrownBy(() -> {
            passwordRepo.save(tokenTooLong);
        }).isInstanceOf(Exception.class); // Should be a data truncation or constraint violation
    }


    // ============ EDGE CASES FOR FIND ============

    @Test
    void shouldHandleSpecialCharactersInToken() {
        // Given
        String specialToken = "token-with-special!@#$%^&*()_+-={}[]|\\:;\"'<>?,./";
        
        PasswordResetToken token = PasswordResetToken.builder()
                .user(testUser)
                .token(specialToken)
                .expiresAt(Instant.now().plusSeconds(3600))
                .used(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        passwordRepo.save(token);

        // When
        Optional<PasswordResetToken> found = passwordRepo.findByToken(specialToken);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo(specialToken);
    }


    /**
     * Test to cover: if (token == null || token.isEmpty()) { return Optional.empty(); }
     */
    @Test
    void shouldReturnEmptyForNullAndEmptyTokens() {
        // Test null token
        Optional<PasswordResetToken> nullResult = passwordRepo.findByToken(null);
        assertThat(nullResult).isEmpty();

        // Test empty string token
        Optional<PasswordResetToken> emptyResult = passwordRepo.findByToken("");
        assertThat(emptyResult).isEmpty();
    }

    /**
     * Test that exercises the database query execution path
     * This ensures the try-catch block is executed, even if we can't easily trigger the exception
     */
    @Test
    void shouldExecuteQueryPathAndHandleNoResults() {
        // This test ensures the query execution path is covered
        // Even though it doesn't trigger an exception, it exercises the try block
        // and the normal "no results found" path
        
        Optional<PasswordResetToken> result = passwordRepo.findByToken("definitely-does-not-exist-token-12345");
        assertThat(result).isEmpty();
        
        // Test with various realistic token formats that won't be found
        String[] nonExistentTokens = {
            "abc123",
            "token-that-does-not-exist",
            "00000000-0000-0000-0000-000000000000",
            "very-long-token-name-that-definitely-should-not-exist-in-database"
        };
        
        for (String token : nonExistentTokens) {
            Optional<PasswordResetToken> tokenResult = passwordRepo.findByToken(token);
            assertThat(tokenResult).isEmpty();
        }
    }

    
}
