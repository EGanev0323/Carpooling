package bg.tu_sofia.carpooling.auth.service;

import bg.tu_sofia.carpooling.auth.domain.RefreshToken;
import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.auth.repository.RefreshTokenRepository;
import bg.tu_sofia.carpooling.common.exception.UnauthorizedException;
import bg.tu_sofia.carpooling.config.AppProperties;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private static final String ISSUER = "carpooling-tu-sofia";
    private static final String CLAIM_ROLES = "roles";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final AppProperties appProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Cache<String, Boolean> jtiBlacklist;

    public JwtService(AppProperties appProperties,
                      RefreshTokenRepository refreshTokenRepository,
                      Cache<String, Boolean> jtiBlacklist) {
        this.appProperties = appProperties;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jtiBlacklist = jtiBlacklist;
        this.algorithm = Algorithm.HMAC512(appProperties.jwt().secret());
        this.verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
    }

    public String issueAccessToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> "ROLE_" + r.getName().name())
                .toList();

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(appProperties.jwt().accessTokenExpiryMinutes() * 60L);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(user.getId().toString())
                .withClaim(CLAIM_ROLES, roles)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .sign(algorithm);
    }

    /**
     * Issues a new refresh token and persists it.
     * Returns a {@link IssuedRefreshToken} containing both the raw token (for the cookie)
     * and the saved entity.
     */
    @Transactional
    public IssuedRefreshToken issueRefreshToken(User user, String ip, String userAgent) {
        String rawToken = generateSecureToken();
        String tokenHash = sha256Hex(rawToken);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusDays(appProperties.jwt().refreshTokenExpiryDays());

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .ip(ip)
                .userAgent(truncate(userAgent, 255))
                .build();

        entity = refreshTokenRepository.save(entity);
        return new IssuedRefreshToken(rawToken, entity);
    }

    public DecodedJWT validateToken(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            if (jtiBlacklist.getIfPresent(decoded.getId()) != null) {
                throw new UnauthorizedException("Token has been revoked");
            }
            return decoded;
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (JWTVerificationException ex) {
            throw new UnauthorizedException("Invalid or expired token: " + ex.getMessage());
        }
    }

    public void blacklistToken(String jti, Instant expiry) {
        if (jti != null && !jti.isBlank()) {
            jtiBlacklist.put(jti, Boolean.TRUE);
            log.debug("Blacklisted JWT JTI: {}", jti);
        }
    }

    public String hashToken(String rawToken) {
        return sha256Hex(rawToken);
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * Value object returned by {@link #issueRefreshToken}.
     */
    public record IssuedRefreshToken(String rawToken, RefreshToken entity) {}
}
