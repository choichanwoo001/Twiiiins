package com.twiiiins.newsletter;

import com.twiiiins.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController @RequestMapping("/api/newsletter") @RequiredArgsConstructor
public class NewsletterController {
    private final SubscriptionService subscriptions;
    private final NewsletterSettings settings;
    private final RequestThrottle throttle;
    private final NewsletterTurnstile turnstile;
    private final SignupCounts counts;
    public record Signup(@NotBlank @Email @Size(max = 254) String email, @Pattern(regexp = "en|de") @NotNull String language, @AssertTrue boolean consent, @Size(max = 2048) String turnstileToken) {}
    public record Link(@NotBlank @Size(max = 1024) String token) {}
    @GetMapping("/status") public ApiResponse<?> status() {
        boolean available = settings.signupReady();
        return ApiResponse.success(available && settings.turnstileEnabled()
            ? Map.of("available", true, "turnstileSiteKey", settings.getTurnstileSiteKey())
            : Map.of("available", available));
    }
    @PostMapping("/subscribe") public ApiResponse<?> subscribe(@Valid @RequestBody Signup request, HttpServletRequest http) {
        if (!settings.signupReady())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Newsletter signups are unavailable.");
        // Remote address is set only by the configured trusted reverse proxy, never a client supplied header.
        if (!throttle.allow("ip:" + http.getRemoteAddr(), 10, 3600)) {
            counts.limited();
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please try again later.");
        }
        switch (turnstile.verify(request.turnstileToken())) {
            case REJECTED -> {
                counts.rejected();
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Could not verify the signup.");
            }
            case UNAVAILABLE -> {
                counts.verificationUnavailable();
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Signup verification is temporarily unavailable.");
            }
            case PASSED -> counts.subscription(subscriptions.subscribe(request.email(), request.language()));
        }
        return ApiResponse.success("Thank you for subscribing. We will send you news from TWIIIINS.");
    }
    @PostMapping("/confirm") public ApiResponse<?> confirm(@Valid @RequestBody Link request) {
        subscriptions.confirm(request.token()); return ApiResponse.success("Subscription confirmed.");
    }
    @PostMapping("/unsubscribe") public ApiResponse<?> unsubscribe(@Valid @RequestBody Link request) {
        subscriptions.unsubscribe(request.token()); return ApiResponse.success("Unsubscribed.");
    }
}
