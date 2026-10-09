package com.twiiiins.newsletter;

import com.twiiiins.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/newsletter") @RequiredArgsConstructor
public class NewsletterController {
    private final SubscriptionService subscriptions;
    private final NewsletterSettings settings;
    private final RequestThrottle throttle;
    public record Signup(@NotBlank @Email @Size(max = 254) String email, @Pattern(regexp = "en|de") @NotNull String language, @AssertTrue boolean consent) {}
    public record Link(@NotBlank @Size(max = 1024) String token) {}
    @GetMapping("/status") public ApiResponse<?> status() { return ApiResponse.success(java.util.Map.of("available", settings.ready())); }
    @PostMapping("/subscribe") public ApiResponse<?> subscribe(@Valid @RequestBody Signup request, HttpServletRequest http) {
        settings.requireReady();
        // Remote address is set only by the configured trusted reverse proxy, never a client supplied header.
        if (!throttle.allow("ip:" + http.getRemoteAddr(), 10, 3600))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "Please try again later.");
        subscriptions.subscribe(request.email(), request.language());
        return ApiResponse.success("Thank you for subscribing. We will send you news from TWIIIINS.");
    }
    @PostMapping("/confirm") public ApiResponse<?> confirm(@Valid @RequestBody Link request) {
        subscriptions.confirm(request.token()); return ApiResponse.success("Subscription confirmed.");
    }
    @PostMapping("/unsubscribe") public ApiResponse<?> unsubscribe(@Valid @RequestBody Link request) {
        subscriptions.unsubscribe(request.token()); return ApiResponse.success("Unsubscribed.");
    }
}
