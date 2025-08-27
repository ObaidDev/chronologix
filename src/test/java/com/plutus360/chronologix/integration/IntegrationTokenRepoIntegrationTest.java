package com.plutus360.chronologix.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.plutus360.chronologix.dao.repositories.IntegrationTokenRepo;
import com.plutus360.chronologix.entities.IntegrationToken;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Testcontainers
@Transactional
class IntegrationTokenRepoIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

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
    private IntegrationTokenRepo integrationTokenRepo;

    @PersistenceContext
    private EntityManager entityManager;

    private IntegrationToken testToken1;
    private IntegrationToken testToken2;
    private IntegrationToken testToken3;
    private OffsetDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = OffsetDateTime.now(ZoneOffset.UTC);
        

        // Create test tokens
        testToken1 = IntegrationToken.builder()
            .name("Test Token 1")
            .tokenHash("hash123abc")
            .active(true)
            .userId(UUID.randomUUID().toString())
            .createdAt(testTime.minusDays(5))
            .updatedAt(testTime.minusDays(1))
            .expiredAt(testTime.plusDays(30))
            .updatedAt(testTime.minusHours(2))
            .build();

        testToken2 = IntegrationToken.builder()
            .name("Test Token 2")
            .tokenHash("hash456def")
            .active(true)
            .userId(UUID.randomUUID().toString())
            .createdAt(testTime.minusDays(3))
            .updatedAt(testTime.minusHours(6))
            .expiredAt(testTime.plusDays(60))
            .updatedAt(testTime.minusMinutes(30))
            .build();

        testToken3 = IntegrationToken.builder()
            .name("Inactive Token")
            .tokenHash("hash789ghi")
            .active(false)
            .userId(UUID.randomUUID().toString())
            .createdAt(testTime.minusDays(10))
            .updatedAt(testTime.minusDays(2))
            .expiredAt(testTime.minusDays(1)) // Expired
            .updatedAt(testTime.minusDays(3))
            .build();

        // Persist test data
        entityManager.persist(testToken1);
        entityManager.persist(testToken2);
        entityManager.persist(testToken3);
        entityManager.flush();
    }


    @Test
    void testInsertInBatch() {

        List<IntegrationToken> tokensToInsert = Arrays.asList(testToken1, testToken2);

        // Act
        List<IntegrationToken> result = integrationTokenRepo.insertInBatch(tokensToInsert);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isNotNull();
        assertThat(result.get(1).getId()).isNotNull();

        // Verify in database
        IntegrationToken savedToken1 = entityManager.find(IntegrationToken.class, result.get(0).getId());
        assertThat(savedToken1.getName()).isEqualTo("Test Token 1");
        assertThat(savedToken1.getTokenHash()).isEqualTo("hash123abc");
    }

    @Test
    void testInsertInBatch_emptyList() {
        // Act
        List<IntegrationToken> result = integrationTokenRepo.insertInBatch(Arrays.asList());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testInsertInBatch_nullList() {
        // Act
        List<IntegrationToken> result = integrationTokenRepo.insertInBatch(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void testFindByIds() {
        // Arrange
        List<Long> ids = Arrays.asList(testToken1.getId(), testToken2.getId());

        // Act
        List<IntegrationToken> result = integrationTokenRepo.findByIds(ids);

        // Assert
        assertThat(result).hasSize(2);

        assertThat(result)
            .extracting(IntegrationToken::getName)
            .containsExactlyInAnyOrder("Test Token 1", "Test Token 2");

        assertThat(result)
            .extracting(IntegrationToken::getTokenHash)
            .containsExactlyInAnyOrder("hash123abc", "hash456def");
    }

    @Test
    void testFindByIds_singleId() {
        // Arrange
        List<Long> ids = Arrays.asList(testToken1.getId());

        // Act
        List<IntegrationToken> result = integrationTokenRepo.findByIds(ids);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Token 1");
        assertThat(result.get(0).getTokenHash()).isEqualTo("hash123abc");
    }

    @Test
    void testFindByIds_nonExistentIds() {
        // Arrange
        List<Long> ids = Arrays.asList(99999L, 88888L);

        // Act
        List<IntegrationToken> result = integrationTokenRepo.findByIds(ids);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByIds_emptyList() {
        // Act
        List<IntegrationToken> result = integrationTokenRepo.findByIds(Arrays.asList());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testFindToken_existingToken() {
        // Act
        Optional<IntegrationToken> result = integrationTokenRepo.findToken("hash123abc");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Token 1");
        assertThat(result.get().getTokenHash()).isEqualTo("hash123abc");
        assertThat(result.get().getActive()).isTrue();
    }

    @Test
    void testFindToken_nonExistentToken() {
        // Act
        Optional<IntegrationToken> result = integrationTokenRepo.findToken("nonexistent-hash");

        // Assert
        assertThat(result).isNotPresent();
    }

    @Test
    void testFindToken_nullToken() {
        // Act
        Optional<IntegrationToken> result = integrationTokenRepo.findToken(null);

        // Assert
        assertThat(result).isNotPresent();
    }

    @Test
    void testFindToken_emptyToken() {
        // Act
        Optional<IntegrationToken> result = integrationTokenRepo.findToken("");

        // Assert
        assertThat(result).isNotPresent();
    }

    @Test
    void testFindToken_inactiveToken() {
        // Act
        Optional<IntegrationToken> result = integrationTokenRepo.findToken("hash789ghi");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Inactive Token");
        assertThat(result.get().getActive()).isFalse();
    }

    // @Test
    void testTokenProperties() {
        // Act
        Optional<IntegrationToken> result = integrationTokenRepo.findToken("hash456def");

        // Assert
        assertThat(result).isPresent();
        IntegrationToken token = result.get();
        
        assertThat(token.getCreatedAt()).isEqualTo(testTime.minusDays(3));
        assertThat(token.getUpdatedAt()).isEqualTo(testTime.minusHours(6));
        assertThat(token.getExpiredAt()).isEqualTo(testTime.plusDays(60));
        assertThat(token.getUpdatedAt()).isEqualTo(testTime.minusMinutes(30));
    }

    @Test
    void testBatchOperationWithLargeData() {
        // Arrange - Create multiple tokens to test batch processing
        List<IntegrationToken> largeTokenList = Arrays.asList(
            createTestToken("large001", "Large Token 1"),
            createTestToken("large002", "Large Token 2"),
            createTestToken("large003", "Large Token 3"),
            createTestToken("large004", "Large Token 4"),
            createTestToken("large005", "Large Token 5")
        );

        // Act
        List<IntegrationToken> result = integrationTokenRepo.insertInBatch(largeTokenList);

        // Assert
        assertThat(result).hasSize(5);
        result.forEach(token -> assertThat(token.getId()).isNotNull());

        // Verify all tokens can be found
        List<Long> ids = result.stream().map(IntegrationToken::getId).toList();
        List<IntegrationToken> foundTokens = integrationTokenRepo.findByIds(ids);
        assertThat(foundTokens).hasSize(5);
    }

    // // Helper method to create test tokens
    private IntegrationToken createTestToken(String hash, String name) {
        return IntegrationToken.builder()
            .name(name)
            .tokenHash(hash)
            .active(true)
            .createdAt(testTime)
            .updatedAt(testTime)
            .expiredAt(testTime.plusDays(30))
            .build();
    }
}