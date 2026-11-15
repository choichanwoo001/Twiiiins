package com.twiiiins.newsletter;

import com.twiiiins.dto.*;
import com.twiiiins.dto.request.*;
import com.twiiiins.service.NewsService;
import com.twiiiins.service.FileUploadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import static java.util.Map.entry;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor @org.springframework.validation.annotation.Validated
public class AdminNewsletterController {
    private final NewsService news;
    private final NewsletterContent content;
    private final MailingService mailingService;
    private final MailingRepository mailings;
    private final DeliveryRepository deliveries;
    private final SubscriberRepository subscribers;
    private final SubscriptionService subscriptions;
    private final NewsletterSettings settings;
    private final NewsletterQueue queue;
    private final FileUploadService uploads;
    private final SignupCounts signupCounts;
    public record Send(@NotNull Long version, Boolean publishToNews, MailingService.Audience expectedAudience) {}
    public record Test(@Pattern(regexp = "en|de") @NotNull String language, @NotBlank @Email String email) {}
    @PostMapping("/newsletter/images") public ApiResponse<?> draftImages(@RequestParam("files") List<MultipartFile> files) {
        if (files.isEmpty() || files.size() > 50) throw new IllegalArgumentException("Choose 1 to 50 images.");
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) urls.add(uploads.uploadImage(file).getUrl());
        return ApiResponse.success(urls);
    }
    @GetMapping("/news") public ApiResponse<?> news(@RequestParam(required = false) @Pattern(regexp = "NEWS|NEWSLETTER") String source) { return ApiResponse.success(news.adminList(source)); }
    @GetMapping("/news/{id}") public ApiResponse<?> news(@PathVariable Long id) { return ApiResponse.success(news.newsletterGet(id)); }
    @PostMapping("/news") public ApiResponse<?> create(@Valid @RequestBody NewsCreateRequest request) { return ApiResponse.success(news.createNews(request)); }
    @PutMapping("/news/{id}") public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody NewsUpdateRequest request) { return ApiResponse.success(news.updateNews(id, request)); }
    @DeleteMapping("/news/{id}") public ApiResponse<?> delete(@PathVariable Long id) { news.deleteNews(id); return ApiResponse.success("Deleted or archived"); }
    @PostMapping("/news/{id}/images") public ApiResponse<?> images(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
        NewsDto item = news.newsletterGet(id);
        List<String> images = new ArrayList<>(item.getImageUrls());
        if (images.size() + files.size() > 50) throw new IllegalArgumentException("Maximum 50 images.");
        for (MultipartFile file : files) images.add(uploads.uploadImage(file).getUrl());
        NewsUpdateRequest request = new NewsUpdateRequest(); request.setImageUrls(images); request.setVersion(item.getVersion());
        return ApiResponse.success(news.updateNews(id, request));
    }
    @PostMapping("/news/{id}/publish") public ApiResponse<?> publish(@PathVariable Long id) { return ApiResponse.success(news.publish(id)); }
    @PostMapping("/news/{id}/send") public ApiResponse<?> send(@PathVariable Long id, @Valid @RequestBody Send request) {
        return ApiResponse.success(mailingService.send(id, request.version(), !Boolean.FALSE.equals(request.publishToNews()), request.expectedAudience()));
    }
    @GetMapping("/news/{id}/preview") public ApiResponse<?> preview(@PathVariable Long id, @RequestParam String language) { return ApiResponse.success(mailingService.preview(id, language)); }
    @PostMapping("/news/{id}/test") public ApiResponse<?> test(@PathVariable Long id, @Valid @RequestBody Test request) { mailingService.test(id, request.language(), request.email()); return ApiResponse.success("Test queued"); }
    // A separate DTO accepts incomplete drafts without relaxing create/publish validation.
    @PostMapping("/newsletter/preview") public ApiResponse<?> previewDraft(@Valid @RequestBody NewsPreviewRequest request) {
        SubscriptionService.language(request.getLanguage());
        com.twiiiins.entity.News item = request.toNews();
        String html = content.render(item, request.getLanguage(), settings.getPublicUrl());
        return ApiResponse.success(Map.of("title", content.title(item, request.getLanguage()),
            "html", content.complete(html, request.getLanguage(), settings, null, null, true)));
    }
    @GetMapping("/newsletter/settings") public ApiResponse<?> settings() {
        NewsletterQueue.Status queueStatus = queue.status();
        return ApiResponse.success(Map.ofEntries(
            entry("available", settings.ready()), entry("testRecipients", settings.getTestRecipients()),
            entry("perMinute", settings.getPerMinute()), entry("dailyLimit", settings.getDailyLimit()),
            entry("paused", queueStatus.paused()), entry("pauseReason", queueStatus.pauseReason()),
            entry("localMailboxUrl", settings.localMailboxUrl()), entry("configurationIssues", settings.configurationIssues()),
            entry("signupCounts", signupCounts.snapshot()), entry("dailyUsage", queueStatus.dailyUsage()),
            entry("signupVerification", !settings.turnstileEnabled() ? "DISABLED" : settings.turnstileConfigured() ? "ENABLED" : "INCOMPLETE")));
    }
    @GetMapping("/newsletter/audience") public ApiResponse<?> audience() {
        return ApiResponse.success(mailingService.audience());
    }
    @PostMapping("/newsletter/resume") public ApiResponse<?> resume() { queue.resume(); return ApiResponse.success("Resumed"); }
    public record SubscriberView(Long id, String email, String language, String status, java.time.Instant createdAt, String restoreStatus) {
        static SubscriberView of(Subscriber s) {
            String restoreStatus = "EXCLUDED".equals(s.getStatus()) ? Objects.toString(s.getStatusBeforeExclusion(), "UNSUBSCRIBED") : null;
            boolean pending = "PENDING".equals(s.getStatus()) || "PENDING".equals(restoreStatus);
            String language = pending && s.getPendingLanguage() != null ? s.getPendingLanguage() : s.getLanguage();
            return new SubscriberView(s.getId(), s.getEmail(), language, s.getStatus(), s.getCreatedAt(), restoreStatus);
        }
    }
    @GetMapping("/newsletter/subscribers") public ApiResponse<?> subscribers(@RequestParam(required = false) String search, @RequestParam(required = false) String language, @RequestParam(required = false) String status) {
        return ApiResponse.success(subscribers.findAll().stream().map(SubscriberView::of)
            .filter(s -> search == null || s.email().contains(search.strip().toLowerCase(Locale.ROOT)))
            .filter(s -> language == null || language.equals(s.language())).filter(s -> status == null || status.equals(s.status()))
            .sorted(Comparator.comparing(SubscriberView::createdAt).reversed()).toList());
    }
    @PostMapping("/newsletter/subscribers/{id}/exclude") public ApiResponse<?> exclude(@PathVariable Long id) { subscriptions.exclude(id); return ApiResponse.success("Excluded"); }
    @PostMapping("/newsletter/subscribers/{id}/restore") public ApiResponse<?> restore(@PathVariable Long id) {
        return ApiResponse.success(SubscriberView.of(subscriptions.restoreExcluded(id)));
    }
    @GetMapping("/newsletter/mailings") public ApiResponse<?> mailings() { return ApiResponse.success(mailings.findAll()); }
    @GetMapping("/newsletter/mailings/{id}/deliveries") public ApiResponse<?> deliveries(@PathVariable Long id) { return ApiResponse.success(deliveries.findByMailingIdOrderByIdAsc(id)); }
    @PostMapping("/newsletter/deliveries/{id}/retry") public ApiResponse<?> retry(@PathVariable Long id) { queue.retry(id); return ApiResponse.success("Retry queued"); }
}
