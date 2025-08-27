package com.plutus360.chronologix.conf;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${CACHE_TTL:600}") // Default TTL in seconds, can be overridden
    private long cacheTtlSeconds;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure Caffeine cache with 600 seconds TTL
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10_000)                                    // Maximum number of entries
            .expireAfterWrite(Duration.ofSeconds(cacheTtlSeconds))  // TTL: 600 seconds (10 minutes)
            .recordStats());                                        // Enable statistics
        
        return cacheManager;
    }
}