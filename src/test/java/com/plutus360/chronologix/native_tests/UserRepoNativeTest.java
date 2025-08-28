package com.plutus360.chronologix.native_tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// import com.plutus360.chronologix.conf.NativeTestConfiguration;
import com.plutus360.chronologix.dao.repositories.UserRepo;
import com.plutus360.chronologix.entities.User;

@SpringBootTest
@Testcontainers
@ActiveProfiles("native")
// @Import(NativeTestConfiguration.class)
@Transactional
class UserRepoNativeTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("native_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false) // Important for native tests
            .withStartupTimeoutSeconds(60);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }


    @Autowired
    private UserRepo userRepo;

    @Test
    void testBasicUserOperations() {
        // Create user with all required fields
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        
        User user = User.builder()
            .username("nativeuser")
            .email("native@test.com")
            .password("password123")
            .build();

        // Test insert (your custom repository method)
        User savedUser = userRepo.insert(user);
        
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("nativeuser");

        // Test findById (your custom repository method)
        User foundUser = userRepo.findById(savedUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("nativeuser");
        assertThat(foundUser.getEmail()).isEqualTo("native@test.com");

        // Test findByUsername (your custom repository method)
        User foundByUsername = userRepo.findByUsername("nativeuser");
        assertThat(foundByUsername).isNotNull();
        assertThat(foundByUsername.getId()).isEqualTo(savedUser.getId());

        // Test findByEmail (your custom repository method)
        Optional<User> foundByEmail = userRepo.findByEmail("native@test.com");
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getId()).isEqualTo(savedUser.getId());

        // Test update (your custom repository method)
        User updatedUser = userRepo.update(foundUser);
        
        assertThat(updatedUser).isNotNull();
    }

    @Test
    void contextLoads() {
        assertThat(userRepo).isNotNull();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void testUserCrudOperations() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        
        // Create
        User user = User.builder()
            .username("cruduser")
            .email("crud@test.com")
            .password("crudpassword")
            .build();

        User savedUser = userRepo.insert(user);
        assertThat(savedUser.getId()).isNotNull();

        // Read
        User foundUser = userRepo.findById(savedUser.getId());
        assertThat(foundUser).isNotNull();

        // Update
        foundUser.setEmail("updated@test.com");
        User updatedUser = userRepo.update(foundUser);
        assertThat(updatedUser.getEmail()).isEqualTo("updated@test.com");

        // Delete (if you have this method)
        boolean deleted = userRepo.deleteById(savedUser.getId());
        assertThat(deleted).isTrue();

        // Verify deletion
        User deletedUser = userRepo.findById(savedUser.getId());
        assertThat(deletedUser).isNull();
    }
}