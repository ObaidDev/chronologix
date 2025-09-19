package com.plutus360.chronologix.integration.repo;

import java.util.Map;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {

        DockerImageName citusImage = DockerImageName.parse("citusdata/citus:12.1")
            .asCompatibleSubstituteFor("postgres");

        /*
        *    old version of testcontainers with postgres 15
        */    
        // POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")


        POSTGRES_CONTAINER = new PostgreSQLContainer<>(citusImage)
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true)
                .withTmpFs(Map.of("/var/lib/postgresql/data", "rw"))
                .withCommand("postgres", 
                           "-c", "max_connections=50",
                           "-c", "shared_buffers=128MB",
                           "-c", "fsync=off",
                           "-c", "synchronous_commit=off",
                           "-c", "full_page_writes=off",
                           "-c", "log_statement=none");

        POSTGRES_CONTAINER.start();

        // Register shutdown hook to ensure proper cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (POSTGRES_CONTAINER.isRunning()) {
                    POSTGRES_CONTAINER.stop();
                }
            } catch (Exception e) {
                // Log but don't throw to avoid preventing JVM shutdown
                System.err.println("Error stopping PostgreSQL container: " + e.getMessage());
            }
        }));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // Optimized settings for faster tests
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.properties.hibernate.jdbc.batch_size", () -> "25");
        registry.add("spring.jpa.properties.hibernate.order_inserts", () -> "true");
        registry.add("spring.jpa.properties.hibernate.order_updates", () -> "true");
        
        // Minimal connection pool
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "2");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "5000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "30000");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "60000");
        
        // Disable verbose logging
        registry.add("logging.level.org.testcontainers", () -> "WARN");
        registry.add("logging.level.com.github.dockerjava", () -> "WARN");
        registry.add("logging.level.org.hibernate.SQL", () -> "WARN");
    }

    protected static PostgreSQLContainer<?> getPostgresContainer() {
        return POSTGRES_CONTAINER;
    }
}