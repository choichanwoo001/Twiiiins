package com.twiiiins.newsletter;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

class NewsletterTurnstileTest {
    @Test void acceptsOnlySuccessfulResponsesForTheSignupActionAndSite() {
        assertEquals(NewsletterTurnstile.Result.PASSED, verify(true, "www.example.com", "newsletter_signup"));
        assertEquals(NewsletterTurnstile.Result.REJECTED, verify(false, "example.com", "newsletter_signup"));
        assertEquals(NewsletterTurnstile.Result.REJECTED, verify(true, "other.example.com", "newsletter_signup"));
        assertEquals(NewsletterTurnstile.Result.REJECTED, verify(true, "example.com", "other_action"));
    }

    private NewsletterTurnstile.Result verify(boolean success, String host, String action) {
        var settings = new NewsletterSettings();
        settings.setPublicUrl("https://example.com");
        settings.setTurnstileSiteKey("public-key");
        settings.setTurnstileSecretKey("private-key");
        var builder = RestClient.builder().requestInterceptor((request, body, execution) -> {
            assertTrue(request.getURI().toString().endsWith("/turnstile/v0/siteverify"));
            assertEquals(MediaType.APPLICATION_JSON, request.getHeaders().getContentType());
            String payload = new String(body, StandardCharsets.UTF_8);
            assertTrue(payload.contains("private-key"));
            assertTrue(payload.contains("example-token"));
            String response = """
                {"success":%s,"hostname":"%s","action":"%s"}
                """.formatted(success, host, action);
            var mock = new MockClientHttpResponse(response.getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
            mock.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return mock;
        });
        return new NewsletterTurnstile(builder, settings).verify("example-token");
    }
}
