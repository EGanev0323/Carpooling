package bg.tu_sofia.carpooling.auth.api;

import bg.tu_sofia.carpooling.auth.api.dto.*;
import bg.tu_sofia.carpooling.auth.service.AuthService;
import bg.tu_sofia.carpooling.auth.service.JwtService;
import bg.tu_sofia.carpooling.common.exception.UnauthorizedException;
import bg.tu_sofia.carpooling.config.AppProperties;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String AUTH_TOKEN_COOKIE = "AUTH_TOKEN";
    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

    private final AuthService authService;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    public AuthController(AuthService authService, JwtService jwtService, AppProperties appProperties) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.appProperties = appProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String ip = resolveClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        UserResponse userResponse = authService.register(request, ip, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = resolveClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        Map<String, Object> result = authService.login(request, ip, userAgent);

        String accessToken = (String) result.get("accessToken");
        String rawRefreshToken = (String) result.get("rawRefreshToken");
        UserResponse user = (UserResponse) result.get("user");

        ResponseCookie authCookie = buildAuthTokenCookie(accessToken,
                appProperties.jwt().accessTokenExpiryMinutes() * 60L);
        ResponseCookie refreshCookie = buildRefreshTokenCookie(rawRefreshToken,
                appProperties.jwt().refreshTokenExpiryDays() * 86400L);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest httpRequest) {
        String rawRefreshToken = extractCookieValue(httpRequest, REFRESH_TOKEN_COOKIE);
        String ip = resolveClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        Map<String, Object> result = authService.refresh(rawRefreshToken, ip, userAgent);

        String newAccessToken = (String) result.get("accessToken");
        String newRawRefreshToken = (String) result.get("rawRefreshToken");

        ResponseCookie authCookie = buildAuthTokenCookie(newAccessToken,
                appProperties.jwt().accessTokenExpiryMinutes() * 60L);
        ResponseCookie refreshCookie = buildRefreshTokenCookie(newRawRefreshToken,
                appProperties.jwt().refreshTokenExpiryDays() * 86400L);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        // Extract current access token JTI for blacklisting
        String accessToken = extractCookieValue(httpRequest, AUTH_TOKEN_COOKIE);
        String jti = null;
        Instant jtiExpiry = null;

        if (accessToken != null) {
            try {
                DecodedJWT decoded = jwtService.validateToken(accessToken);
                jti = decoded.getId();
                jtiExpiry = decoded.getExpiresAtAsInstant();
            } catch (Exception ex) {
                // Token already invalid — proceed with logout anyway
            }
        }

        String rawRefreshToken = extractCookieValue(httpRequest, REFRESH_TOKEN_COOKIE);
        authService.logout(jti, jtiExpiry, rawRefreshToken);

        // Clear cookies
        ResponseCookie clearAuth = buildClearedCookie(AUTH_TOKEN_COOKIE, "/");
        ResponseCookie clearRefresh = buildClearedCookie(REFRESH_TOKEN_COOKIE, "/api/v1/auth/refresh");

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearAuth.toString())
                .header(HttpHeaders.SET_COOKIE, clearRefresh.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return ResponseEntity.ok(authService.me(userId));
    }

    private ResponseCookie buildAuthTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(AUTH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/v1/auth/refresh")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie buildClearedCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(path)
                .maxAge(0)
                .build();
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
