package com.twiiiins.newsletter;

import com.twiiiins.security.Tokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Uses the caller's transaction and mail-gate lock for changes and delivery claims. */
@Component
@RequiredArgsConstructor
class NewsletterMailboxes {
    private static final String PERSONAL_GMAIL = "@gmail.com";
    private static final DeliveryRecipient BLOCKED = new DeliveryRecipient(false, null);

    private final SubscriberRepository subscribers;
    private final DeliveryRepository deliveries;

    Optional<Subscriber> subscribe(String address, String language, Instant now) {
        String email = normalize(address);
        List<Subscriber> aliases = findAliases(emailKey(email));
        if (hasExclusion(aliases) || preferred(aliases) != null) return Optional.empty();

        Subscriber subscriber = aliases.stream()
            .min(Comparator.comparingInt(NewsletterMailboxes::signupPriority))
            .orElseGet(Subscriber::new);
        if (subscriber.getId() == null) {
            subscriber.setCreatedAt(now);
            subscriber.setEmail(email);
        }
        activate(subscriber, language, "newsletter-v2-single-opt-in", now);
        rotateUnsubscribeLinks(aliases, subscriber);
        subscriber.setRequestedAt(now);
        return Optional.of(subscribers.saveAndFlush(subscriber));
    }

    void confirm(Subscriber subscriber, Instant now) {
        List<Subscriber> aliases = findAliases(emailKey(subscriber.getEmail()));
        if (subscriber.getConfirmationExpires() == null || !subscriber.getConfirmationExpires().isAfter(now)
            || hasExclusion(aliases)) {
            throw new IllegalArgumentException("Invalid, used or expired link.");
        }
        boolean renewing = !"ACTIVE".equals(subscriber.getStatus()) || preferred(aliases) == null;
        activate(subscriber, subscriber.getPendingLanguage(), "newsletter-v1", now);
        if (renewing) rotateUnsubscribeLinks(aliases, subscriber);
    }

    void unsubscribe(Subscriber subscriber, Instant now) {
        for (Subscriber alias : findAliases(emailKey(subscriber.getEmail()))) {
            if (!hasOptedOut(alias)) optOut(alias, now, now);
        }
    }

    void exclude(Subscriber subscriber, Instant now) {
        for (Subscriber alias : findAliases(emailKey(subscriber.getEmail()))) {
            if ("EXCLUDED".equals(alias.getStatus())) continue;
            alias.setStatusBeforeExclusion(alias.getStatus());
            alias.setStatus("EXCLUDED");
            alias.setUpdatedAt(now);
        }
    }

    void restoreExcluded(Subscriber subscriber, Instant now) {
        if (!"EXCLUDED".equals(subscriber.getStatus())) return;
        for (Subscriber alias : findAliases(emailKey(subscriber.getEmail()))) {
            for (Delivery delivery : deliveries.findBySubscriberIdAndStatus(alias.getId(), "WAITING")) {
                delivery.markSkipped(now);
            }
            if (!"EXCLUDED".equals(alias.getStatus())) continue;
            // Older records without a previous state must opt in again.
            String previousStatus = alias.getStatusBeforeExclusion();
            alias.setStatus(previousStatus == null ? "UNSUBSCRIBED" : previousStatus);
            alias.setStatusBeforeExclusion(null);
            alias.setUpdatedAt(now);
        }
    }

    List<Subscriber> recipients() {
        Map<String, List<Subscriber>> byMailbox = new LinkedHashMap<>();
        for (Subscriber subscriber : subscribers.findAll()) {
            byMailbox.computeIfAbsent(emailKey(subscriber.getEmail()), key -> new ArrayList<>()).add(subscriber);
        }
        return byMailbox.values().stream()
            .filter(aliases -> !hasExclusion(aliases))
            .map(NewsletterMailboxes::preferred)
            .filter(Objects::nonNull)
            .toList();
    }

    record DeliveryRecipient(boolean allowed, Subscriber subscriber) {}

    DeliveryRecipient recipientFor(Delivery delivery, Instant now) {
        Subscriber subscriber = delivery.getSubscriberId() == null ? null
            : subscribers.lockById(delivery.getSubscriberId()).orElse(null);
        String key = subscriber == null ? "" : emailKey(subscriber.getEmail());
        List<Subscriber> gmailAliases = key.endsWith(PERSONAL_GMAIL) ? findAliases(key) : List.of();
        if (hasExclusion(gmailAliases)) return BLOCKED;

        boolean allowed = switch (delivery.getKind()) {
            case "WELCOME" -> hasCurrentSubscription(delivery, subscriber);
            case "CONFIRM" -> subscriber != null
                && subscriber.getConfirmationExpires() != null
                && subscriber.getConfirmationExpires().isAfter(now)
                && Objects.equals(delivery.getConfirmationContext(), subscriber.getConfirmationContext())
                && !"EXCLUDED".equals(subscriber.getStatus());
            case "NEWS" -> hasCurrentSubscription(delivery, subscriber) && (gmailAliases.isEmpty()
                || canDeliverToGmail(delivery, subscriber, key, gmailAliases));
            default -> true;
        };
        return allowed ? new DeliveryRecipient(true, subscriber) : BLOCKED;
    }

    void beforeRemoval(Subscriber subscriber, Instant now) {
        if (!hasOptedOut(subscriber)) return;
        String key = emailKey(subscriber.getEmail());
        if (!key.endsWith(PERSONAL_GMAIL)) return;
        List<Subscriber> aliases = findAliases(key);
        if (preferred(aliases) != null) return;
        for (Subscriber alias : aliases) {
            if ("ACTIVE".equals(alias.getStatus())) optOut(alias, now, null);
        }
    }

    static String emailKey(String address) {
        String normalized = normalize(address);
        if (!normalized.endsWith(PERSONAL_GMAIL)) return normalized;
        int separator = normalized.lastIndexOf('@');
        return normalized.substring(0, separator).replace(".", "") + normalized.substring(separator);
    }

    private List<Subscriber> findAliases(String key) {
        List<Subscriber> aliases = new ArrayList<>();
        subscribers.findByEmailKey(key).ifPresent(aliases::add);
        subscribers.findByEmailKeyIsNullOrderById().stream()
            .filter(subscriber -> key.equals(emailKey(subscriber.getEmail())))
            .forEach(aliases::add);
        return aliases;
    }

    private static String normalize(String address) {
        return address.strip().toLowerCase(Locale.ROOT);
    }

    private boolean canDeliverToGmail(Delivery delivery, Subscriber subscriber, String key, List<Subscriber> aliases) {
        Subscriber recipient = preferred(aliases);
        return recipient != null && Objects.equals(recipient.getId(), subscriber.getId())
            && deliveries.countOtherSentToGmail(delivery.getMailingId(), delivery.getId(), key.replace(".", "")) == 0;
    }

    private static boolean hasCurrentSubscription(Delivery delivery, Subscriber subscriber) {
        return subscriber != null && "ACTIVE".equals(subscriber.getStatus())
            && Objects.equals(delivery.getSubscriptionVersion(), subscriber.getUnsubscribeVersion());
    }

    private static Subscriber preferred(List<Subscriber> aliases) {
        Subscriber latest = aliases.stream()
            .filter(subscriber -> "ACTIVE".equals(subscriber.getStatus()))
            .max(Comparator.comparing(Subscriber::getConsentAt, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(Subscriber::getId))
            .orElse(null);
        if (latest == null) return null;
        for (Subscriber subscriber : aliases) {
            if (blocks(subscriber, latest.getConsentAt())) return null;
        }
        return latest;
    }

    private static boolean hasExclusion(List<Subscriber> aliases) {
        return aliases.stream().anyMatch(subscriber -> "EXCLUDED".equals(subscriber.getStatus()));
    }

    private static boolean hasOptedOut(Subscriber subscriber) {
        return "UNSUBSCRIBED".equals(subscriber.getStatus())
            || "UNSUBSCRIBED".equals(subscriber.getStatusBeforeExclusion());
    }

    private static boolean blocks(Subscriber subscriber, Instant consentAt) {
        if (!hasOptedOut(subscriber)) return false;
        // For old rows, the last update is an upper bound on when the opt-out occurred.
        Instant optOut = subscriber.getUnsubscribedAt() == null ? subscriber.getUpdatedAt() : subscriber.getUnsubscribedAt();
        return consentAt == null || optOut == null || !consentAt.isAfter(optOut);
    }

    private static int signupPriority(Subscriber subscriber) {
        return switch (subscriber.getStatus()) {
            case "ACTIVE" -> 0;
            case "UNSUBSCRIBED" -> 1;
            default -> 2;
        };
    }

    private static void activate(Subscriber subscriber, String language, String consentVersion, Instant now) {
        subscriber.setLanguage(language);
        subscriber.setStatus("ACTIVE");
        subscriber.setConsentAt(now);
        subscriber.setUnsubscribedAt(null);
        subscriber.setConsentVersion(consentVersion);
        subscriber.setUpdatedAt(now);
        subscriber.clearConfirmation();
    }

    private static void rotateUnsubscribeLinks(List<Subscriber> aliases, Subscriber subscriber) {
        for (Subscriber alias : aliases) {
            if (alias != subscriber) alias.setUnsubscribeVersion(Tokens.random());
        }
        subscriber.setUnsubscribeVersion(Tokens.random());
    }

    private static void optOut(Subscriber subscriber, Instant now, Instant unsubscribedAt) {
        if ("EXCLUDED".equals(subscriber.getStatus())) subscriber.setStatusBeforeExclusion("UNSUBSCRIBED");
        else subscriber.setStatus("UNSUBSCRIBED");
        subscriber.setUpdatedAt(now);
        subscriber.setUnsubscribedAt(unsubscribedAt);
        subscriber.clearConfirmation();
    }
}
