package bg.tu_sofia.carpooling.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    private final AppProperties appProperties;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(AppProperties appProperties, JwtAuthFilter jwtAuthFilter) {
        this.appProperties = appProperties;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Argon2id password encoder.
     * Params: saltLength=16, hashLength=32, parallelism=4, memory=65536 KiB, iterations=3
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 4, 65536, 3);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF: cookie-based, readable by JS (for Axios XSRF-TOKEN header)
        CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepo)
                .csrfTokenRequestHandler(csrfHandler)
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/auth/register",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/verify-email"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                // Public read endpoints
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/cities",
                        "/api/v1/rides",
                        "/api/v1/rides/{publicId}",
                        "/api/v1/users/{publicId}",
                        "/actuator/health",
                        "/api/docs/**",
                        "/api/swagger-ui/**",
                        "/api/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll()
                // Ride search (also covered above, but explicit for clarity)
                .requestMatchers(HttpMethod.GET, "/api/v1/rides/**").permitAll()
                // Admin endpoints
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // Disable default form login and HTTP Basic — we use JWT cookies
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/problem+json");
                    response.getWriter().write("""
                            {"type":"about:blank","title":"Unauthorized","status":401,
                             "detail":"Authentication required","path":"%s"}
                            """.formatted(request.getRequestURI()));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/problem+json");
                    response.getWriter().write("""
                            {"type":"about:blank","title":"Forbidden","status":403,
                             "detail":"Access denied","path":"%s"}
                            """.formatted(request.getRequestURI()));
                })
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(appProperties.cors().allowedOrigin()));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("X-CSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
