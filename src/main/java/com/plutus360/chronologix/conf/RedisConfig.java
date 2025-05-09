package com.plutus360.chronologix.conf;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class RedisConfig {

    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private int redisPort;

    @Value("${REDIS_USER}")
    private String redisUser;

    @Value("${REDIS_PASSWORD}")
    private String redisPassword;

    @Value("${CACHE_TTL:60}") // Default TTL in seconds, can be overridden
    private long cacheTtlSeconds;



    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);

        if (redisUser != null && !redisUser.isEmpty()) {
            redisConfig.setUsername(redisUser);
        }

        redisConfig.setPassword(RedisPassword.of(redisPassword));

        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory connectionFactory) {
        
        try {
            connectionFactory.getConnection().ping();
        } catch (Exception e) {
            throw new IllegalStateException("❌ Failed to connect to Redis: " + e.getMessage(), e);
        }
    
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(cacheTtlSeconds));
    
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
}
