package bg.tu_sofia.carpooling.auth.service;

import bg.tu_sofia.carpooling.common.exception.BusinessException;
import bg.tu_sofia.carpooling.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TurnstileService {

    private static final Logger log = LoggerFactory.getLogger(TurnstileService.class);

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;

    public TurnstileService(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Validates a Cloudflare Turnstile token.
     * If turnstile is disabled (dev/test), always returns true.
     */
    public void validate(String turnstileToken) {
        if (!appProperties.turnstile().enabled()) {
            log.debug("Turnstile validation disabled — skipping");
            return;
        }

        if (turnstileToken == null || turnstileToken.isBlank()) {
            throw new BusinessException("Turnstile token is required");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", appProperties.turnstile().secret());
            body.add("response", turnstileToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    appProperties.turnstile().verifyUrl(),
                    request,
                    Map.class
            );

            if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
                log.warn("Turnstile verification failed: {}", response);
                throw new BusinessException("Invalid Turnstile token");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Turnstile verification error: {}", ex.getMessage());
            throw new BusinessException("Turnstile verification failed");
        }
    }
}
