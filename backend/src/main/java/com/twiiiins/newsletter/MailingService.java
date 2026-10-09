package com.twiiiins.newsletter;

import com.twiiiins.entity.News;
import com.twiiiins.repository.NewsRepository;
import com.twiiiins.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class MailingService {
    private final NewsRepository news;
    private final NewsService newsService;
    private final MailingRepository mailings;
    private final SubscriberRepository subscribers;
    private final DeliveryRepository deliveries;
    private final NewsletterContent content;
    private final NewsletterSettings settings;
    private final MailGateRepository gates;

    @Transactional public Mailing send(Long newsId, Long version) {
        settings.requireReady();
        if (gates.lock().isPaused()) throw new IllegalArgumentException("SMTP is paused. Check settings and resume first.");
        News item = news.lockById(newsId).orElseThrow(() -> new IllegalArgumentException("News not found"));
        newsService.requireSource(item, News.NEWSLETTER);
        Optional<Mailing> existing = mailings.findByNewsId(newsId);
        if (existing.isPresent()) return existing.get();
        if (!Objects.equals(item.getVersion(), version)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "News changed. Reload before sending.");
        if (item.isArchived()) throw new IllegalArgumentException("Archived news cannot be sent");
        newsService.validatePublication(item);
        item.setStatus("PUBLISHED");
        Mailing mailing = new Mailing();
        mailing.setNewsId(newsId);
        mailing.setTitleEn(item.getTitle()); mailing.setTitleDe(item.getTitleDe());
        mailing.setHtmlEn(content.render(item, "en", settings.getPublicUrl()));
        mailing.setHtmlDe(content.render(item, "de", settings.getPublicUrl()));
        mailing.setImages(String.join("\n", item.getImageUrls()));
        List<Subscriber> recipients = subscribers.findByStatus("ACTIVE");
        mailing.setTotalCount(recipients.size());
        mailings.saveAndFlush(mailing);
        for (Subscriber subscriber : recipients) {
            Delivery delivery = new Delivery();
            delivery.setKind("NEWS"); delivery.setMailingId(mailing.getId());
            delivery.setSubscriberId(subscriber.getId()); delivery.setEmail(subscriber.getEmail());
            delivery.setLanguage(subscriber.getLanguage());
            delivery.setSubscriptionVersion(subscriber.getUnsubscribeVersion());
            deliveries.save(delivery);
        }
        return mailing;
    }
    @Transactional(readOnly = true) public Map<String, Object> preview(Long newsId, String language) {
        SubscriptionService.language(language);
        News item = newsService.getEntity(newsId);
        newsService.requireSource(item, News.NEWSLETTER);
        return Map.of("title", "de".equals(language) ? Objects.toString(item.getTitleDe(), "") : item.getTitle(),
            "html", content.complete(content.render(item, language, settings.getPublicUrl()), language, settings, null, null, true));
    }
    @Transactional public void test(Long newsId, String language, String email) {
        settings.requireReady();
        if (!settings.getTestRecipients().contains(email)) throw new IllegalArgumentException("Choose a configured test recipient.");
        MailGate gate = gates.lock();
        if (gate.isPaused()) throw new IllegalArgumentException("SMTP is paused.");
        Map<String, Object> preview = preview(newsId, language);
        Delivery delivery = new Delivery();
        delivery.setKind("TEST"); delivery.setEmail(email); delivery.setLanguage(language);
        delivery.setTestTitle("[TEST] " + preview.get("title")); delivery.setTestHtml((String) preview.get("html"));
        deliveries.save(delivery);
    }
    @Transactional public void retry(Long id) {
        gates.lock();
        Delivery delivery = deliveries.findById(id).orElseThrow(() -> new IllegalArgumentException("Delivery not found"));
        if (!"FAILED".equals(delivery.getStatus()) || !"NEWS".equals(delivery.getKind())) throw new IllegalArgumentException("Only definitely failed newsletter deliveries can be retried.");
        delivery.setStatus("WAITING"); delivery.setAttempts(0); delivery.setErrorCode(null);
        delivery.setNextAttempt(java.time.Instant.now()); delivery.setFinishedAt(null);
    }
    @Transactional public void resume() {
        settings.requireReady();
        MailGate gate = gates.lock(); gate.setPaused(false); gate.setReason(null);
    }
}
