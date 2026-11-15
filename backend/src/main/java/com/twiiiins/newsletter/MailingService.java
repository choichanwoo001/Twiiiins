package com.twiiiins.newsletter;

import com.twiiiins.entity.News;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.repository.NewsRepository;
import com.twiiiins.service.NewsPublication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class MailingService {
    public record Audience(long total, long en, long de) {
        private static Audience of(List<Subscriber> recipients) {
            long english = recipients.stream().filter(subscriber -> "en".equals(subscriber.getLanguage())).count();
            return new Audience(recipients.size(), english, recipients.size() - english);
        }
    }
    private final java.time.Clock clock;
    private final NewsRepository news;
    private final NewsPublication publication;
    private final MailingRepository mailings;
    private final NewsletterMailboxes mailboxes;
    private final DeliveryRepository deliveries;
    private final NewsletterContent content;
    private final NewsletterSettings settings;
    private final MailGateRepository gates;

    @Transactional public Mailing send(Long newsId, Long version) {
        return send(newsId, version, true);
    }

    @Transactional public Mailing send(Long newsId, Long version, boolean publishToNews) {
        return send(newsId, version, publishToNews, null);
    }

    @Transactional public Mailing send(Long newsId, Long version, boolean publishToNews, Audience expectedAudience) {
        MailGate gate = gates.lock();
        News item = news.lockById(newsId).orElseThrow(() -> new IllegalArgumentException("News not found"));
        publication.requireSource(item, News.NEWSLETTER);
        Optional<Mailing> existing = mailings.findByNewsId(newsId);
        if (existing.isPresent()) {
            Mailing mailing = existing.get();
            if (mailing.isPublishToNews() != publishToNews)
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "This newsletter was already queued with a different publishing option.");
            return mailing;
        }
        settings.requireReady();
        if (gate.isPaused()) throw new IllegalArgumentException("SMTP is paused. Check settings and resume first.");
        if (!Objects.equals(item.getVersion(), version)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "News changed. Reload before sending.");
        publication.prepareMailing(item, publishToNews);
        List<Subscriber> recipients = mailboxes.recipients();
        if (expectedAudience != null && !expectedAudience.equals(Audience.of(recipients))) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
                "발송 대상 수가 변경되었습니다. 다시 확인하세요.");
        }
        Mailing mailing = new Mailing(); mailing.setCreatedAt(clock.instant());
        mailing.setNewsId(newsId);
        mailing.setPublishToNews(publishToNews);
        mailing.setTitleEn(content.title(item, "en")); mailing.setTitleDe(content.title(item, "de"));
        mailing.setHtmlEn(content.render(item, "en", settings.getPublicUrl()));
        mailing.setHtmlDe(content.render(item, "de", settings.getPublicUrl()));
        mailing.setImages(String.join("\n", item.getImageUrls()));
        mailing.setTotalCount(recipients.size());
        mailings.saveAndFlush(mailing);
        for (Subscriber subscriber : recipients) {
            Delivery delivery = new Delivery();
            delivery.setCreatedAt(clock.instant()); delivery.setNextAttempt(clock.instant());
            delivery.setKind("NEWS"); delivery.setMailingId(mailing.getId());
            delivery.setSubscriberId(subscriber.getId()); delivery.setEmail(subscriber.getEmail());
            delivery.setLanguage(subscriber.getLanguage());
            delivery.setSubscriptionVersion(subscriber.getUnsubscribeVersion());
            deliveries.save(delivery);
        }
        return mailing;
    }

    @Transactional(readOnly = true) public Audience audience() {
        return Audience.of(mailboxes.recipients());
    }
    @Transactional(readOnly = true) public Map<String, Object> preview(Long newsId, String language) {
        SubscriptionService.language(language);
        News item = news.findById(newsId).orElseThrow(() -> new ResourceNotFoundException("News not found"));
        publication.requireSource(item, News.NEWSLETTER);
        return Map.of("title", content.title(item, language),
            "html", content.complete(content.render(item, language, settings.getPublicUrl()), language, settings, null, null, true));
    }
    @Transactional public void test(Long newsId, String language, String email) {
        settings.requireReady();
        if (!settings.getTestRecipients().contains(email)) throw new IllegalArgumentException("Choose a configured test recipient.");
        MailGate gate = gates.lock();
        if (gate.isPaused()) throw new IllegalArgumentException("SMTP is paused.");
        Map<String, Object> preview = preview(newsId, language);
        Delivery delivery = new Delivery();
        delivery.setCreatedAt(clock.instant()); delivery.setNextAttempt(clock.instant());
        delivery.setKind("TEST"); delivery.setEmail(email); delivery.setLanguage(language);
        delivery.setTestTitle("[TEST] " + preview.get("title")); delivery.setTestHtml((String) preview.get("html"));
        deliveries.save(delivery);
    }
}
