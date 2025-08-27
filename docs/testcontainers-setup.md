# Testcontainers Setup with Hibernate (Without Spring Data JPA)

This document explains how to set up integration tests using Testcontainers with direct Hibernate configuration (without Spring Data JPA) and how to organize test profiles.

## Overview

Our application uses **Hibernate directly** rather than Spring Data JPA, which requires a specific approach for integration testing with Testcontainers.

## Project Structure

```
src/
├── main/java/
│   └── com/plutus360/chronologix/
│       ├── dao/repositories/        # Hibernate repositories
│       ├── entities/               # JPA entities
│       └── conf/                   # Hibernate configuration
├── test/java/
│   ├── integration/                # Integration tests (with containers)
│   └── tests/                      # Unit tests (with H2)
└── test/resources/
    └── application-test.properties  # Test configuration
```

## Test Profiles Configuration

### 1. Test Properties (`src/test/resources/application-test.properties`)

```properties
####################################
#   Test database configuration    #
####################################

# Default H2 configuration (overridden by Testcontainers)
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.batch_size=10

# JWT Configuration (test values)
security.jwt.secret-key=test-jwt-secret-key-for-testing-only-not-production-use
security.jwt.expiration-time=3600000

# Cache configuration
CACHE_TTL=600
```

### 2. Maven Profiles for Different Test Types

Add to your `pom.xml`:

```xml
<profiles>
    <!-- Unit tests only (fast, H2) -->
    <profile>
        <id>unit-tests</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <includes>
                            <include>**/tests/**/*Test.java</include>
                        </includes>
                        <excludes>
                            <exclude>**/integration/**</exclude>
                        </excludes>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
    
    <!-- Integration tests only (slower, PostgreSQL containers) -->
    <profile>
        <id>integration-tests</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <includes>
                            <include>**/integration/**/*Test.java</include>
                        </includes>
                        <excludes>
                            <exclude>**/tests/**</exclude>
                        </excludes>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

## Testcontainers with Direct Hibernate

### Key Differences from Spring Data JPA

Since we use Hibernate directly instead of Spring Data JPA:

1. **Use `@SpringBootTest`** instead of `@DataJpaTest`
2. **Inject `EntityManager`** directly with `@PersistenceContext`
3. **Manual transaction management** with `@Transactional`
4. **Custom repository implementations** using EntityManager

### Integration Test Example

```java
package com.plutus360.chronologix.integration;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.plutus360.chronologix.dao.repositories.DeviceRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest                           // ✅ Use this, not @DataJpaTest
@Testcontainers                          // ✅ Enable Testcontainers
@Transactional                           // ✅ Manage transactions manually
@ActiveProfiles("test")                  // ✅ Load test properties
class DeviceRepoIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Override properties to use the container
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private DeviceRepo deviceRepo;        // ✅ Your custom repository

    @PersistenceContext
    private EntityManager entityManager;  // ✅ Direct Hibernate access

    @Test
    void testDatabaseConnection() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(deviceRepo).isNotNull();
        assertThat(entityManager).isNotNull();
    }

    // Your test methods...
}
```

### Unit Test Example (H2)

```java
package com.plutus360.chronologix.tests;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.plutus360.chronologix.dao.repositories.DeviceRepo;

@DataJpaTest                             // ✅ OK for H2 unit tests
@Import(DeviceRepo.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class DeviceRepoUnitTest {
    // Fast unit tests with H2
}
```

## Required Dependencies

Add to your `pom.xml`:

```xml
<dependencies>
    <!-- Test dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Running Tests

### Command Examples

```bash
# Run all tests
mvn test

# Run unit tests only (fast, H2)
mvn test -Punit-tests

# Run integration tests only (slower, PostgreSQL containers)
mvn test -Pintegration-tests

# Run specific integration test
mvn test -Dtest=DeviceRepoIntegrationTest

# Run with specific profile
mvn test -Dspring.profiles.active=test

# Run integration tests with pattern
mvn test -Dtest="*Integration*"
```

### Performance Comparison

| Test Type | Database | Startup Time | Execution Time | Use Case |
|-----------|----------|--------------|----------------|-----------|
| Unit Tests | H2 | ~1-2 sec | Fast | Basic CRUD, Business Logic |
| Integration Tests | PostgreSQL Container | ~10-15 sec | Slower | JSONB queries, Full functionality |

## Key Benefits

1. **Real Database Testing**: Integration tests use actual PostgreSQL with JSONB support
2. **Fast Feedback**: Unit tests with H2 for quick development cycles  
3. **Isolation**: Each test gets a fresh database container
4. **CI/CD Ready**: Can run different test suites at different pipeline stages
5. **JSONB Support**: Only PostgreSQL containers support JSONB field selection

## Best Practices

1. **Use H2 for unit tests** - Fast feedback for basic functionality
2. **Use PostgreSQL containers for integration tests** - Full feature testing
3. **Separate test classes** - Different packages for different test types
4. **Profile-based configuration** - Easy switching between test environments
5. **Container reuse** - One container per test class for performance

## Troubleshooting

### Common Issues

1. **JWT_SECRET not found**: Add JWT properties to `application-test.properties`
2. **Test discovery failure**: Ensure proper annotations and dependencies
3. **Container startup issues**: Check Docker is running and accessible
4. **JSONB queries fail in H2**: Use PostgreSQL container for JSONB testing

### Debug Commands

```bash
# Check dependencies
mvn dependency:tree | grep -E "(junit|testcontainers)"

# Check compiled test classes
ls -la target/test-classes/com/plutus360/chronologix/

# Run with debug logging
mvn test -X -Dtest=DeviceRepoIntegrationTest
```

---

This setup provides a robust testing strategy that leverages both fast unit tests and comprehensive integration tests while working with direct Hibernate configuration.






https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/