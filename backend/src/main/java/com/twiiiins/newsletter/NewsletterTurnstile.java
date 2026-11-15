package com.twiiiins.newsletter;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class NewsletterTurnstile {
    static final String ACTION = "newsletter_signup";
    private static final String VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    private final RestClient client;
    private final NewsletterSettings settings;

    NewsletterTurnstile(RestClient.Builder builder, NewsletterSettings settings) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.client = builder.requestFactory(requestFactory).build();
        this.settings = settings;
    }

    record Verification(boolean success, String hostname, String action, @JsonProperty("error-codes") List<String> errors) {}
    enum Result { PASSED, REJECTED, UNAVAILABLE }

    Result verify(String token) {
        if (!settings.turnstileEnabled()) return Result.PASSED;
        if (token == null || token.isBlank()) return Result.REJECTED;
        Verification result;
        try {
            result = client.post().uri(VERIFY_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("secret", settings.getTurnstileSecretKey(), "response", token))
                .retrieve().body(Verification.class);
        } catch (RestClientException e) {
            return Result.UNAVAILABLE;
        }
        if (result == null || (result.errors() != null && result.errors().stream()
            .anyMatch(List.of("internal-error", "invalid-input-secret", "missing-input-secret")::contains))) return Result.UNAVAILABLE;
        String host = URI.create(settings.getPublicUrl()).getHost().replaceFirst("(?i)^www\\.", "");
        return result.success() && ACTION.equals(result.action())
            && (host.equalsIgnoreCase(result.hostname()) || ("www." + host).equalsIgnoreCase(result.hostname()))
            ? Result.PASSED : Result.REJECTED;
    }
}
