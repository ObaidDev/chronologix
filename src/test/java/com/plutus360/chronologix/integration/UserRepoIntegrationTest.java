package com.plutus360.chronologix.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.plutus360.chronologix.dao.repositories.UserRepo;
import com.plutus360.chronologix.entities.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
class UserRepoIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false);

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
    private UserRepo userRepo ;

    @PersistenceContext
    private EntityManager entityManager;



    private User testUser1;
    private User testUser2;
    private User testUser3;


   @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = User.builder()
            .username("testuser1")
            .email("testuser1@example.com")
            .password("hashedpassword123")
            .build();

        testUser2 = User.builder()
            .username("testuser2")
            .email("testuser2@example.com")
            .password("hashedpassword456")
            .build();

        testUser3 = User.builder()
            .username("inactiveuser")
            .email("inactive@example.com")
            .password("hashedpassword789")
            .build();

        // Persist test users
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.persist(testUser3);
        entityManager.flush();
    }



    @Test
    void testInsert_validUser() {

        // Act
        User result = userRepo.insert(testUser1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser1");
        assertThat(result.getEmail()).isEqualTo("testuser1@example.com");

        // Verify in database
        User savedUser = entityManager.find(User.class, result.getId());
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser1");
    }


    @Test
    void testInsert_nullUser() {
        // Act
        User result = userRepo.insert(null);

        // Assert
        assertThat(result).isNull();
    }



    @Test
    void testInsertTowUsers() {
        // Arrange
        // Act
        User result = userRepo.insert(testUser2);
        User result2 = userRepo.insert(testUser3);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        assertThat(result2).isNotNull();
        assertThat(result2.getId()).isNotNull();
    }




    @Test
    void testUpdate_existingUser() {

        // Arrange - modify existing user
        testUser1.setEmail("updated@example.com");
        testUser2.setEmail("hello@gmail.com");

        testUser1.setPassword("newhashedpassword");
        testUser2.setPassword("anotherhashedpassword");

        // Act
        User result = userRepo.update(testUser1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser1.getId());
        assertThat(result.getEmail()).isEqualTo("updated@example.com");

        // Verify in database
        entityManager.flush();
        entityManager.clear();
        User updatedUser = entityManager.find(User.class, testUser1.getId());
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }


    @Test
    void testUpdate_nullUser() {
        // Act
        User result = userRepo.update(null);

        // Assert
        assertThat(result).isNull();
    }



    /**
     * 
     * find user by id
     */

    @Test
    void testFindById_existingUser() {
        // Act
        User result = userRepo.findById(testUser1.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser1.getId());
        assertThat(result.getUsername()).isEqualTo("testuser1");
        assertThat(result.getEmail()).isEqualTo("testuser1@example.com");

    }


    @Test
    void testFindById_nonExistentUser() {
        // Act
        User result = userRepo.findById(99999L);

        // Assert
        assertThat(result).isNull();
    }


    @Test
    void testFindById_nullId() {
        // Act
        User result = userRepo.findById(null);

        // Assert
        assertThat(result).isNull();
    }


    /*
     * 
     * FIND USER BY USERNAME
     */


    @Test
    void testFindByUsernameExistingUser() {
        // Act
        User result = userRepo.findByUsername("testuser2");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser2");
        assertThat(result.getEmail()).isEqualTo("testuser2@example.com");
    }


    @Test
    void testFindByUsernameNonExistentUser() {
        // Act
        User result = userRepo.findByUsername("nonexistent");

        // Assert
        assertThat(result).isNull();
    }


    @Test
    void testFindByUsernameNullUsername() {
        // Act
        User result = userRepo.findByUsername(null);

        // Assert
        assertThat(result).isNull();
    }


    @Test
    void testFindByUsernameCaseSensitive() {
        // Act
        User result = userRepo.findByUsername("TESTUSER1");

        // Assert
        assertThat(result).isNull(); // Should be case sensitive
    }



    /**
     *
     * FIND USER BY EMAIL
     */


    @Test
    void testFindByEmail_existingUser() {
        // Act
        Optional<User> result = userRepo.findByEmail("testuser1@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("testuser1@example.com");
        assertThat(result.get().getUsername()).isEqualTo("testuser1");
    }


    @Test
    void testFindByEmailNonExistentUser() {
        // Act
        Optional<User> result = userRepo.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(result).isNotPresent();
    }


    @Test
    void testFindByEmailNullEmail() {
        // Act
        Optional<User> result = userRepo.findByEmail(null);

        // Assert
        assertThat(result).isNotPresent();
    }




    /*
     * 
     * DELETE USER
     */


    @Test
    void testDeleteById_existingUser() {
        // Arrange
        Long userIdToDelete = testUser1.getId();

        // Act
        boolean result = userRepo.deleteById(userIdToDelete);

        // Assert
        assertThat(result).isTrue();

        // Verify user is deleted
        entityManager.flush();
        entityManager.clear();
        User deletedUser = entityManager.find(User.class, userIdToDelete);
        assertThat(deletedUser).isNull();

        // Verify other users still exist
        User otherUser = entityManager.find(User.class, testUser2.getId());
        assertThat(otherUser).isNotNull();
    }

    @Test
    void testDeleteById_nonExistentUser() {
        // Act
        boolean result = userRepo.deleteById(99999L);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testDeleteById_nullId() {
        // Act
        boolean result = userRepo.deleteById(null);

        // Assert
        assertThat(result).isFalse();
    }



    /**
     * 
     * FIND ALL USERS
     */


    @Test
    void testFindAll() {
        // Act
        List<User> result = userRepo.findAll();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3); // testUser1, testUser2, testUser3

        List<String> usernames = result.stream()
            .map(User::getUsername)
            .toList();
        assertThat(usernames).containsExactlyInAnyOrder("testuser1", "testuser2", "inactiveuser");

        List<String> emails = result.stream()
            .map(User::getEmail)
            .toList();
        assertThat(emails).containsExactlyInAnyOrder(
            "testuser1@example.com", 
            "testuser2@example.com", 
            "inactive@example.com"
        );
    }

}
