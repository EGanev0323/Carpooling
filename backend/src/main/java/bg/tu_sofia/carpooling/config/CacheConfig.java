package bg.tu_sofia.carpooling.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    /**
     * Named cache manager for Spring @Cacheable annotations (e.g. "cities").
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("cities");
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofHours(1)));
        return manager;
    }

    /**
     * Caffeine cache for JWT JTI blacklisting.
     * Entries expire after 20 minutes (slightly longer than access token TTL of 15 min).
     */
    @Bean
    public Cache<String, Boolean> jtiBlacklist() {
        return Caffeine.newBuilder()
                .maximumSize(50_000)
                .expireAfterWrite(Duration.ofMinutes(20))
                .build();
    }

    /**
     * Caffeine cache for rate limiting buckets (Bucket4j).
     * Entries expire after 1 hour (matches longest rate limit window).
     */
    @Bean
    public Cache<String, Bucket> bucketCache() {
        return Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterAccess(Duration.ofHours(1))
                .build();
    }
}
