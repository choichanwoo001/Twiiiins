package com.twiiiins.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeminiTranslatorTest {
    private final GeminiTranslator client = new GeminiTranslator(new TranslationSettings(), new ObjectMapper());
    private String envelope(String value) throws Exception {
        return "{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[{\"text\":" + new ObjectMapper().writeValueAsString(value) + "}]}}]}";
    }
    @Test void parsesStructuredTranslation() throws Exception {
        var result = client.parse(envelope("{\"sourceLanguage\":\"de\",\"en\":\"Hello\",\"de\":\"Hallo\"}"));
        assertEquals("de", result.sourceLanguage()); assertEquals("Hello", result.en());
    }
    @Test void rejectsMalformedBlockedIncompleteAndMissingFields() throws Exception {
        for (String value : new String[]{"{}", "{\"candidates\":[]}", "not json", envelope("{}"),
            envelope("{\"sourceLanguage\":\"ko\",\"en\":\"Hello\",\"de\":\"Hallo\"}"),
            envelope("{\"sourceLanguage\":\"en\",\"en\":\"Hello\",\"de\":\"\"}"),
            envelope("{\"sourceLanguage\":\"en\",\"en\":\"Hello\",\"de\":\"Hallo\",\"extra\":1}")})
            assertThrows(GeminiTranslator.Failure.class, () -> client.parse(value));
    }
}
