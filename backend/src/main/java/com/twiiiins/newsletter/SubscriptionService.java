package com.twiiiins.newsletter;

import com.twiiiins.security.Tokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class SubscriptionService {
    private final java.time.Clock clock;
    private final SubscriberRepository subscribers;
    private final NewsletterMailboxes mailboxes;
    private final DeliveryRepository deliveries;
    private final NewsletterSettings settings;
    private final MailGateRepository gate;
    public static String language(String value) {
        if (!"en".equals(value) && !"de".equals(value)) throw new IllegalArgumentException("Choose English or German.");
        return value;
    }
    @Transactional
    public boolean subscribe(String email, String language) {
        settings.requireReady();
        language(language);
        gate.lock(); // serialize subscription changes and welcome-mail creation
        Subscriber subscriber = mailboxes.subscribe(email, language, clock.instant()).orElse(null);
        if (subscriber == null) return false;
        Delivery delivery = new Delivery();
        delivery.setCreatedAt(clock.instant()); delivery.setNextAttempt(clock.instant());
        delivery.setKind("WELCOME");
        delivery.setEmail(subscriber.getEmail());
        delivery.setLanguage(language);
        delivery.setSubscriberId(subscriber.getId());
        delivery.setSubscriptionVersion(subscriber.getUnsubscribeVersion());
        deliveries.save(delivery);
        return true;
    }

    @Transactional
    public void confirm(String token) {
        Tokens.verify(token, settings.getTokenKey());
        gate.lock();
        Subscriber subscriber = subscribers.findByConfirmationHash(Tokens.hash(token))
            .orElseThrow(() -> new IllegalArgumentException("Invalid, used or expired link."));
        mailboxes.confirm(subscriber, clock.instant());
    }
    public String unsubscribeToken(Subscriber subscriber) {
        return Tokens.sign("unsubscribe|" + subscriber.getId() + "|" + subscriber.getUnsubscribeVersion(), settings.getTokenKey());
    }
    @Transactional
    public void unsubscribe(String token) {
        String[] context = Tokens.verify(token, settings.getTokenKey()).split("\\|");
        if (context.length != 3 || !"unsubscribe".equals(context[0])) throw new IllegalArgumentException("Invalid link.");
        gate.lock();
        Subscriber subscriber = subscribers.lockById(Long.valueOf(context[1])).orElseThrow(() -> new IllegalArgumentException("Invalid link."));
        if (!context[2].equals(subscriber.getUnsubscribeVersion())) throw new IllegalArgumentException("Invalid link.");
        mailboxes.unsubscribe(subscriber, clock.instant());
    }
    @Transactional
    public void exclude(Long id) {
        gate.lock();
        Subscriber subscriber = subscribers.lockById(id).orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
        mailboxes.exclude(subscriber, clock.instant());
    }

    @Transactional
    public Subscriber restoreExcluded(Long id) {
        gate.lock();
        Subscriber subscriber = subscribers.lockById(id).orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
        mailboxes.restoreExcluded(subscriber, clock.instant());
        return subscriber;
    }
}
