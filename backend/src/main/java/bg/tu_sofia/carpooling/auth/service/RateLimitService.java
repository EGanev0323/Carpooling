package bg.tu_sofia.carpooling.auth.service;

import bg.tu_sofia.carpooling.common.exception.BusinessException;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private static final int LOGIN_MAX_ATTEMPTS = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);

    private static final int REGISTER_MAX_ATTEMPTS = 3;
    private static final Duration REGISTER_WINDOW = Duration.ofHours(1);

    private final Cache<String, Bucket> bucketCache;

    public RateLimitService(Cache<String, Bucket> bucketCache) {
        this.bucketCache = bucketCache;
    }

    /**
     * Rate limit login attempts: 5 per 15 minutes per (ip + email).
     */
    public void checkLoginRateLimit(String ip, String email) {
        String key = "login:" + ip + ":" + email.toLowerCase();
        Bucket bucket = bucketCache.get(key, k -> buildBucket(LOGIN_MAX_ATTEMPTS, LOGIN_WINDOW));

        if (!bucket.tryConsume(1)) {
            long waitSeconds = LOGIN_WINDOW.toSeconds();
            log.warn("Login rate limit exceeded for key: {}", key);
            throw new BusinessException(
                    "Too many login attempts. Please try again after " + waitSeconds + " seconds.",
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );
        }
    }

    /**
     * Rate limit register attempts: 3 per hour per ip.
     */
    public void checkRegisterRateLimit(String ip) {
        String key = "register:" + ip;
        Bucket bucket = bucketCache.get(key, k -> buildBucket(REGISTER_MAX_ATTEMPTS, REGISTER_WINDOW));

        if (!bucket.tryConsume(1)) {
            long waitSeconds = REGISTER_WINDOW.toSeconds();
            log.warn("Register rate limit exceeded for IP: {}", ip);
            throw new BusinessException(
                    "Too many registration attempts. Please try again after " + waitSeconds + " seconds.",
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );
        }
    }

    private Bucket buildBucket(int capacity, Duration window) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, window)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
