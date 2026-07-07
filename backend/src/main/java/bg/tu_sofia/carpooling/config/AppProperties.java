package bg.tu_sofia.carpooling.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
        @Valid @NotNull JwtProperties jwt,
        @Valid @NotNull TurnstileProperties turnstile,
        @Valid @NotNull CorsProperties cors,
        @Valid @NotNull FrontendProperties frontend,
        @Valid @NotNull SeedProperties seed
) {

    public record JwtProperties(
            @NotBlank String secret,
            @Min(1) int accessTokenExpiryMinutes,
            @Min(1) int refreshTokenExpiryDays
    ) {}

    public record TurnstileProperties(
            @NotBlank String secret,
            @NotBlank String verifyUrl,
            boolean enabled
    ) {}

    public record CorsProperties(
            @NotBlank String allowedOrigin
    ) {}

    public record FrontendProperties(
            @NotBlank String baseUrl
    ) {}

    public record SeedProperties(
            boolean enabled
    ) {}
}
