package com.twiiiins.newsletter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class NewsletterSettingsTest {
    private NewsletterSettings valid() {
        NewsletterSettings settings = new NewsletterSettings();
        settings.setEnabled(true);
        settings.setSmtpHost("smtp.example.com");
        settings.setTokenKey("test-key-not-for-production-1234567890");
        settings.setFrom("news@example.com");
        settings.setReplyTo("reply@example.com");
        settings.setOperator("Test operator");
        return settings;
    }

    @Test void readyMeansNoConfigurationIssues() {
        NewsletterSettings settings = valid();
        assertTrue(settings.ready());
        assertTrue(settings.configurationIssues().isEmpty());
        settings.setPublicUrl("http://localhost:8080");
        assertTrue(settings.ready());
    }

    @TestFactory Stream<DynamicTest> reportsEachMissingConditionWithoutItsValue() {
        Map<String, Consumer<NewsletterSettings>> invalid = Map.ofEntries(
            Map.entry("NEWSLETTER_DISABLED", s -> s.setEnabled(false)),
            Map.entry("SMTP_HOST_MISSING", s -> s.setSmtpHost(null)),
            Map.entry("TOKEN_KEY_MISSING", s -> s.setTokenKey(null)),
            Map.entry("TOKEN_KEY_TOO_SHORT", s -> s.setTokenKey("short-secret")),
            Map.entry("FROM_MISSING", s -> s.setFrom(" ")),
            Map.entry("REPLY_TO_MISSING", s -> s.setReplyTo(null)),
            Map.entry("OPERATOR_MISSING", s -> s.setOperator("")),
            Map.entry("PUBLIC_URL_INVALID", s -> s.setPublicUrl("http://example.com")),
            Map.entry("PER_MINUTE_INVALID", s -> s.setPerMinute(0)),
            Map.entry("DAILY_LIMIT_INVALID", s -> s.setDailyLimit(-1))
        );
        return invalid.entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> {
            NewsletterSettings settings = valid();
            entry.getValue().accept(settings);
            assertEquals(java.util.List.of(entry.getKey()), settings.configurationIssues());
            assertFalse(settings.ready());
            assertFalse(settings.configurationIssues().toString().contains("secret"));
            assertThrows(org.springframework.web.server.ResponseStatusException.class, settings::requireReady);
        }));
    }

    @Test void nullPublicUrlAndBlankKeyHaveSafeDiagnostics() {
        NewsletterSettings settings = valid();
        settings.setPublicUrl(null);
        settings.setTokenKey(" ");
        assertEquals(java.util.List.of("TOKEN_KEY_MISSING", "PUBLIC_URL_INVALID"), settings.configurationIssues());
        assertFalse(settings.ready());
    }
}
