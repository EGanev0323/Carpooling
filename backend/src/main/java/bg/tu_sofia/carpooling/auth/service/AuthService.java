package bg.tu_sofia.carpooling.auth.service;

import bg.tu_sofia.carpooling.auth.api.dto.LoginRequest;
import bg.tu_sofia.carpooling.auth.api.dto.RegisterRequest;
import bg.tu_sofia.carpooling.auth.api.dto.UserResponse;
import bg.tu_sofia.carpooling.auth.domain.*;
import bg.tu_sofia.carpooling.auth.repository.*;
import bg.tu_sofia.carpooling.auth.service.JwtService.IssuedRefreshToken;
import bg.tu_sofia.carpooling.common.exception.BusinessException;
import bg.tu_sofia.carpooling.common.exception.ConflictException;
import bg.tu_sofia.carpooling.common.exception.ResourceNotFoundException;
import bg.tu_sofia.carpooling.common.exception.UnauthorizedException;
import bg.tu_sofia.carpooling.config.AppProperties;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int EMAIL_TOKEN_EXPIRY_HOURS = 24;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TurnstileService turnstileService;
    private final RateLimitService rateLimitService;
    private final AuditService auditService;
    private final AppProperties appProperties;
    private final JavaMailSender mailSender;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       TurnstileService turnstileService,
                       RateLimitService rateLimitService,
                       AuditService auditService,
                       AppProperties appProperties,
                       JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.turnstileService = turnstileService;
        this.rateLimitService = rateLimitService;
        this.auditService = auditService;
        this.appProperties = appProperties;
        this.mailSender = mailSender;
    }

    @Transactional
    public UserResponse register(RegisterRequest request, String ip, String userAgent) {
        turnstileService.validate(request.turnstileToken());
        rateLimitService.checkRegisterRateLimit(ip);

        if (userRepository.existsActiveByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new BusinessException(
                        "Default role not found. Database seeding may be incomplete."));

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .status("ACTIVE")
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        user = userRepository.save(user);

        sendVerificationEmail(user);

        auditService.log(user.getId(), "USER_REGISTERED", "USER", user.getId(),
                Map.of("email", user.getEmail(), "ip", ip));

        return toUserResponse(user);
    }

    @Transactional
    public Map<String, Object> login(LoginRequest request, String ip, String userAgent) {
        turnstileService.validate(request.turnstileToken());
        rateLimitService.checkLoginRateLimit(ip, request.email());

        User user = userRepository.findActiveByEmail(request.email()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditService.log(null, "LOGIN_FAILED", "USER", null,
                    Map.of("email", request.email(), "ip", ip, "reason", "bad_credentials"));
            throw new UnauthorizedException("Invalid email or password");
        }

        if ("BLOCKED".equals(user.getStatus())) {
            auditService.log(user.getId(), "LOGIN_FAILED", "USER", user.getId(),
                    Map.of("ip", ip, "reason", "account_blocked"));
            throw new BusinessException("Your account has been blocked. Please contact support.");
        }

        String accessToken = jwtService.issueAccessToken(user);
        IssuedRefreshToken issued = jwtService.issueRefreshToken(user, ip, userAgent);

        auditService.log(user.getId(), "LOGIN_SUCCESS", "USER", user.getId(),
                Map.of("ip", ip));

        return Map.of(
                "accessToken", accessToken,
                "rawRefreshToken", issued.rawToken(),
                "user", toUserResponse(user)
        );
    }

    @Transactional
    public Map<String, Object> refresh(String rawRefreshToken, String ip, String userAgent) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        String tokenHash = jwtService.hashToken(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (existing.isRevoked()) {
            // Possible token reuse attack — revoke all sessions for the user
            refreshTokenRepository.revokeAllByUserId(existing.getUser().getId(), OffsetDateTime.now());
            auditService.log(existing.getUser().getId(), "REFRESH_TOKEN_REUSE_DETECTED",
                    "REFRESH_TOKEN", existing.getId(), Map.of("ip", ip));
            throw new UnauthorizedException("Refresh token reuse detected. All sessions revoked.");
        }

        if (existing.isExpired()) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        // Revoke old token before issuing new one
        existing.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        String newAccessToken = jwtService.issueAccessToken(user);
        IssuedRefreshToken issued = jwtService.issueRefreshToken(user, ip, userAgent);

        // Record which token replaced this one
        existing.setReplacedById(issued.entity().getId());
        refreshTokenRepository.save(existing);

        return Map.of(
                "accessToken", newAccessToken,
                "rawRefreshToken", issued.rawToken()
        );
    }

    @Transactional
    public void logout(String jti, Instant jtiExpiry, String rawRefreshToken) {
        if (jti != null && !jti.isBlank()) {
            jwtService.blacklistToken(jti, jtiExpiry);
        }

        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = jwtService.hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
                if (!rt.isRevoked()) {
                    rt.setRevokedAt(OffsetDateTime.now());
                    refreshTokenRepository.save(rt);
                    auditService.log(rt.getUser().getId(), "LOGOUT", "USER",
                            rt.getUser().getId(), Map.of());
                }
            });
        }
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        String tokenHash = sha256Hex(rawToken);
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Invalid or expired verification token"));

        if (verificationToken.isUsed()) {
            throw new BusinessException("Verification token already used");
        }

        if (verificationToken.isExpired()) {
            throw new BusinessException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerifiedAt(OffsetDateTime.now());
        userRepository.save(user);

        verificationToken.setUsedAt(OffsetDateTime.now());
        emailVerificationTokenRepository.save(verificationToken);

        auditService.log(user.getId(), "EMAIL_VERIFIED", "USER", user.getId(),
                Map.of("email", user.getEmail()));
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return toUserResponse(user);
    }

    private void sendVerificationEmail(User user) {
        String rawToken = generateSecureToken();
        String tokenHash = sha256Hex(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusHours(EMAIL_TOKEN_EXPIRY_HOURS))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        String verifyLink = appProperties.frontend().baseUrl() + "/verify-email?token=" + rawToken;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Потвърди своя имейл — Carpooling TU Sofia");
            helper.setText(buildVerificationEmailHtml(user.getFirstName(), verifyLink), true);
            mailSender.send(message);
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception ex) {
            // SMTP not configured — log verification link at INFO level for dev convenience
            log.info("Email verification link for {} (SMTP unavailable): {}", user.getEmail(), verifyLink);
        }
    }

    private String buildVerificationEmailHtml(String firstName, String verifyLink) {
        return """
                <html><body style="font-family:sans-serif;max-width:600px;margin:auto">
                <h2>Здравей, %s!</h2>
                <p>Моля потвърди своя имейл адрес, като натиснеш бутона по-долу:</p>
                <p>
                  <a href="%s"
                     style="display:inline-block;background:#2563EB;color:white;
                            padding:12px 28px;text-decoration:none;border-radius:6px;font-size:16px">
                    Потвърди имейл
                  </a>
                </p>
                <p style="color:#6B7280">Линкът е валиден %d часа.</p>
                <p style="color:#6B7280;font-size:12px">
                  Ако не си се регистрирал в Carpooling TU Sofia, просто игнорирай този имейл.
                </p>
                </body></html>
                """.formatted(firstName, verifyLink, EMAIL_TOKEN_EXPIRY_HOURS);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getStatus(),
                user.getEmailVerifiedAt() != null,
                user.getCreatedAt(),
                user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .toList()
        );
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
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
}
