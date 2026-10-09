package com.twiiiins.newsletter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.net.URI;
import java.util.List;

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
    public boolean ready() {
        try {
            URI uri = URI.create(publicUrl);
            return enabled && tokenKey.length() >= 32 && !smtpHost.isBlank() && !from.isBlank()
                && !replyTo.isBlank() && !operator.isBlank() && uri.getHost() != null
                && ("https".equals(uri.getScheme()) || "localhost".equals(uri.getHost()))
                && perMinute > 0 && dailyLimit > 0;
        } catch (Exception e) { return false; }
    }
    public void requireReady() {
        if (!ready()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Newsletter is not available yet.");
    }
}
