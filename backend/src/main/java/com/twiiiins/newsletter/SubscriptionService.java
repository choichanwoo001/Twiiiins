package com.twiiiins.newsletter;

import com.twiiiins.security.Tokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Locale;

@Service @RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriberRepository subscribers;
    private final DeliveryRepository deliveries;
    private final NewsletterSettings settings;
    private final MailGateRepository gate;
    public static String language(String value) {
        if (!"en".equals(value) && !"de".equals(value)) throw new IllegalArgumentException("Choose English or German.");
        return value;
    }
    @Transactional
    public void subscribe(String email, String language) {
        settings.requireReady();
        language(language);
        gate.lock(); // serialize subscription changes and welcome-mail creation
        email = email.strip().toLowerCase(Locale.ROOT);
        Subscriber subscriber = subscribers.findByEmail(email).orElseGet(Subscriber::new);
        Instant now = Instant.now();
        if ("EXCLUDED".equals(subscriber.getStatus())) return;
        if ("ACTIVE".equals(subscriber.getStatus()) && language.equals(subscriber.getLanguage())) return;
        boolean alreadyActive = "ACTIVE".equals(subscriber.getStatus());
        boolean sendWelcome = !alreadyActive || subscriber.getRequestedAt() == null || !subscriber.getRequestedAt().isAfter(now.minusSeconds(600));
        subscriber.setEmail(email);
        subscriber.setLanguage(language);
        subscriber.setStatus("ACTIVE");
        subscriber.setConsentAt(now);
        subscriber.setConsentVersion("newsletter-v2-single-opt-in");
        if (!alreadyActive) subscriber.setUnsubscribeVersion(Tokens.random());
        clearConfirmation(subscriber);
        subscriber.setRequestedAt(now);
        subscriber.setUpdatedAt(now);
        subscribers.saveAndFlush(subscriber);
        if (!sendWelcome) return;
        Delivery delivery = new Delivery();
        delivery.setKind("WELCOME");
        delivery.setEmail(email);
        delivery.setLanguage(language);
        delivery.setSubscriberId(subscriber.getId());
        delivery.setSubscriptionVersion(subscriber.getUnsubscribeVersion());
        deliveries.save(delivery);
    }
    @Transactional
    public void confirm(String token) {
        Tokens.verify(token, settings.getTokenKey());
        gate.lock();
        Subscriber subscriber = subscribers.findByConfirmationHash(Tokens.hash(token))
            .orElseThrow(() -> new IllegalArgumentException("Invalid, used or expired link."));
        if (subscriber.getConfirmationExpires() == null || !subscriber.getConfirmationExpires().isAfter(Instant.now()) || "EXCLUDED".equals(subscriber.getStatus()))
            throw new IllegalArgumentException("Invalid, used or expired link.");
        boolean alreadyActive = "ACTIVE".equals(subscriber.getStatus());
        subscriber.setLanguage(subscriber.getPendingLanguage());
        subscriber.setPendingLanguage(null);
        subscriber.setStatus("ACTIVE");
        subscriber.setConsentAt(Instant.now());
        subscriber.setConsentVersion("newsletter-v1");
        subscriber.setUpdatedAt(Instant.now());
        if (!alreadyActive) subscriber.setUnsubscribeVersion(Tokens.random());
        clearConfirmation(subscriber);
    }
    public String unsubscribeToken(Subscriber subscriber) {
        return Tokens.sign("unsubscribe|" + subscriber.getId() + "|" + subscriber.getUnsubscribeVersion(), settings.getTokenKey());
    }
    @Transactional
    public void unsubscribe(String token) {
        String[] context = Tokens.verify(token, settings.getTokenKey()).split("\\|");
        if (context.length != 3 || !"unsubscribe".equals(context[0])) throw new IllegalArgumentException("Invalid link.");
        Subscriber subscriber = subscribers.lockById(Long.valueOf(context[1])).orElseThrow(() -> new IllegalArgumentException("Invalid link."));
        if (!context[2].equals(subscriber.getUnsubscribeVersion())) throw new IllegalArgumentException("Invalid link.");
        if (!"EXCLUDED".equals(subscriber.getStatus())) subscriber.setStatus("UNSUBSCRIBED");
        subscriber.setUpdatedAt(Instant.now());
        clearConfirmation(subscriber);
    }
    @Transactional
    public void exclude(Long id) {
        Subscriber subscriber = subscribers.lockById(id).orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
        subscriber.setStatus("EXCLUDED");
        subscriber.setUpdatedAt(Instant.now());
        clearConfirmation(subscriber);
    }
    private void clearConfirmation(Subscriber s) {
        s.setConfirmationHash(null); s.setConfirmationContext(null); s.setConfirmationExpires(null); s.setPendingLanguage(null);
    }
}
