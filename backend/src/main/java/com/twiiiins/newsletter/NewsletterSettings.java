package com.twiiiins.newsletter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

@Component @ConfigurationProperties(prefix = "app.newsletter") @Getter @Setter
public class NewsletterSettings {
    private boolean enabled = false;
    private String tokenKey = "";
    private String publicUrl = "https://twiiiins.com";
    private String from = "";
    private String replyTo = "";
    private String operator = "";
    private List<String> testRecipients = List.of();
    private String localMailboxUrl = "";
    public String localMailboxUrl() {
        try {
            URI site = URI.create(publicUrl), mailbox = URI.create(localMailboxUrl);
            return enabled && List.of("localhost", "127.0.0.1").contains(site.getHost())
                && List.of("localhost", "127.0.0.1").contains(mailbox.getHost())
                && List.of("http", "https").contains(mailbox.getScheme()) ? localMailboxUrl : "";
        } catch (Exception e) { return ""; }
    }
    private int perMinute = 10;
    private int dailyLimit = 300;
    @Value("${spring.mail.host:}") private String smtpHost;
    public List<String> configurationIssues() {
        List<String> issues = new ArrayList<>();
        if (!enabled) issues.add("NEWSLETTER_DISABLED");
        if (blank(smtpHost)) issues.add("SMTP_HOST_MISSING");
        if (blank(tokenKey)) issues.add("TOKEN_KEY_MISSING");
        else if (tokenKey.length() < 32) issues.add("TOKEN_KEY_TOO_SHORT");
        if (blank(from)) issues.add("FROM_MISSING");
        if (blank(replyTo)) issues.add("REPLY_TO_MISSING");
        if (blank(operator)) issues.add("OPERATOR_MISSING");
        try {
            URI uri = URI.create(publicUrl);
            if (uri.getHost() == null || !("https".equals(uri.getScheme()) || "localhost".equals(uri.getHost())))
                issues.add("PUBLIC_URL_INVALID");
        } catch (Exception e) { issues.add("PUBLIC_URL_INVALID"); }
        if (perMinute <= 0) issues.add("PER_MINUTE_INVALID");
        if (dailyLimit <= 0) issues.add("DAILY_LIMIT_INVALID");
        return List.copyOf(issues);
    }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    public boolean ready() { return configurationIssues().isEmpty(); }
    public void requireReady() {
        if (!ready()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Newsletter is not available yet.");
    }
}
