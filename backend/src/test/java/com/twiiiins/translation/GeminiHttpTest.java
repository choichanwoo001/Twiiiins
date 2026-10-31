package com.twiiiins.translation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
class GeminiHttpTest {
    HttpServer server; TranslationSettings settings; GeminiTranslator translator;
    AtomicReference<String> request=new AtomicReference<>(); int status=200; String body;
    @BeforeEach void setup() throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/v1beta/models/test-model:generateContent", exchange->{
            request.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            assertEquals("test-key",exchange.getRequestHeaders().getFirst("x-goog-api-key"));
            byte[] bytes=body.getBytes(StandardCharsets.UTF_8); exchange.sendResponseHeaders(status,bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        }); server.start(); settings=new TranslationSettings(); settings.setApiKey("test-key"); settings.setModel("test-model");
        settings.setBaseUrl("http://127.0.0.1:"+server.getAddress().getPort()); translator=new GeminiTranslator(settings,new ObjectMapper());
        body="{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[{\"text\":\"{\\\"sourceLanguage\\\":\\\"en\\\",\\\"en\\\":\\\"Hello\\\",\\\"de\\\":\\\"Hallo\\\"}\"}]}}]}";
    }
    @AfterEach void close(){server.stop(0);}
    ContentTranslation row(){var row=new ContentTranslation();row.setSourceText("Hello");return row;}
    @Test void sendsStructuredRequestAndParsesResponse() throws Exception {
        assertEquals("Hallo",translator.translate(row()).de());
        var sent=new ObjectMapper().readTree(request.get());
        assertEquals("application/json",sent.path("generationConfig").path("responseMimeType").asText());
        assertTrue(request.get().contains("Hello"));
    }
    @Test void rateLimitsAreRetryableButInvalidCredentialsAreNot(){
        status=429;assertTrue(assertThrows(GeminiTranslator.Failure.class,()->translator.translate(row())).retryable);
        status=401;assertFalse(assertThrows(GeminiTranslator.Failure.class,()->translator.translate(row())).retryable);
    }
    @Test void malformedResponsesAndConnectionFailureAreRetryable(){
        body="not-json"; assertTrue(assertThrows(GeminiTranslator.Failure.class,()->translator.translate(row())).retryable);
        server.stop(0); assertEquals("GEMINI_CONNECTION",assertThrows(GeminiTranslator.Failure.class,()->translator.translate(row())).code);
    }
    @Test void stalledTransportTimesOutWithoutCallingTheRealProvider() throws Exception {
        server.stop(0);
        try(var socket=new java.net.ServerSocket(0,1,java.net.InetAddress.getByName("127.0.0.1"))) {
            settings.setBaseUrl("http://127.0.0.1:"+socket.getLocalPort()); settings.setTimeoutSeconds(1);
            // A listening TCP socket that never accepts/answers is a deterministic timeout fixture.
            assertEquals("GEMINI_CONNECTION",assertThrows(GeminiTranslator.Failure.class,()->translator.translate(row())).code);
        }
    }
}
