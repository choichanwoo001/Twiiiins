package com.twiiiins.translation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.translation")
public class TranslationSettings {
    private boolean enabled;
    private String apiKey = "";
    private String model = "gemini-3.5-flash-lite";
    private int timeoutSeconds = 30;
    public boolean available() { return enabled && apiKey != null && !apiKey.isBlank(); }
}
