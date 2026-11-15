package com.twiiiins.newsletter;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
class NewsletterQueue implements ApplicationRunner {
    enum Outcome { ACCEPTED, AUTH, TRANSIENT, FAILED, UNKNOWN }

    record DailyUsage(long used, long remaining, Instant resetsAt) {}
    record Status(boolean paused, String pauseReason, DailyUsage dailyUsage) {}

    private final Clock clock;
    private final NewsletterSettings settings;
    private final DeliveryRepository deliveries;
    private final MailAttemptRepository attempts;
    private final MailGateRepository gates;
    private final NewsletterMailboxes mailboxes;
    private final TransactionTemplate transactions;

    @Override
    public void run(ApplicationArguments args) {
        transactions.executeWithoutResult(tx -> {
            if (!gates.existsById(1L)) gates.saveAndFlush(new MailGate());
            // One application instance owns the queue. Never resend a message interrupted during SMTP.
            for (Delivery delivery : deliveries.findByStatus("SENDING")) {
                delivery.setStatus("UNKNOWN");
                delivery.setErrorCode("RESTART_DURING_SMTP");
                delivery.setFinishedAt(clock.instant());
            }
        });
    }

    // Prepare the envelope while the claim is locked. Only a successful preparation consumes an attempt.
    <T> T claim(BiFunction<Delivery, Subscriber, T> prepareEnvelope) {
        return transactions.execute(tx -> {
            if (!settings.ready() || gates.lock().isPaused()) return null;
            Instant now = clock.instant();
            if (attempts.countByAttemptedAtGreaterThanEqual(now.minusSeconds(60)) >= settings.getPerMinute()
                || dailyUsage(now).remaining() == 0) return null;

            var candidates = deliveries.next(now, PageRequest.of(0, 1));
            if (candidates.isEmpty()) return null;
            Delivery delivery = candidates.get(0);
            var recipient = mailboxes.recipientFor(delivery, now);
            if (!recipient.allowed()) {
                delivery.markSkipped(clock.instant());
                return null;
            }

            T envelope = Objects.requireNonNull(prepareEnvelope.apply(delivery, recipient.subscriber()));
            delivery.setStatus("SENDING");
            delivery.setAttemptedAt(now);
            delivery.setAttempts(delivery.getAttempts() + 1);
            MailAttempt attempt = new MailAttempt();
            attempt.setDeliveryId(delivery.getId());
            attempt.setAttemptedAt(now);
            attempts.save(attempt);
            return envelope;
        });
    }

    void finish(Long id, Outcome outcome, String errorCode) {
        transactions.executeWithoutResult(tx -> {
            MailGate gate = gates.lock();
            Delivery delivery = deliveries.findById(id).orElseThrow();
            delivery.setErrorCode(errorCode);
            if (outcome == Outcome.AUTH) {
                gate.setPaused(true);
                gate.setReason("SMTP_AUTH");
                delivery.setStatus("WAITING");
            } else if (outcome == Outcome.TRANSIENT && delivery.getAttempts() <= 3) {
                delivery.setStatus("WAITING");
                long delay = 60L * (long) Math.pow(5, delivery.getAttempts() - 1);
                delivery.setNextAttempt(clock.instant().plusSeconds(delay));
            } else {
                delivery.setStatus(outcome == Outcome.TRANSIENT ? "FAILED" : outcome.name());
                delivery.setFinishedAt(clock.instant());
            }
        });
    }

    void retry(Long id) {
        transactions.executeWithoutResult(tx -> {
            gates.lock();
            Delivery delivery = deliveries.findById(id).orElseThrow(() -> new IllegalArgumentException("Delivery not found"));
            if (!"FAILED".equals(delivery.getStatus()) || !"NEWS".equals(delivery.getKind())) {
                throw new IllegalArgumentException("Only definitely failed newsletter deliveries can be retried.");
            }
            delivery.setStatus("WAITING");
            delivery.setAttempts(0);
            delivery.setErrorCode(null);
            delivery.setNextAttempt(clock.instant());
            delivery.setFinishedAt(null);
        });
    }

    void resume() {
        transactions.executeWithoutResult(tx -> {
            settings.requireReady();
            MailGate gate = gates.lock();
            gate.setPaused(false);
            gate.setReason(null);
        });
    }

    Status status() {
        MailGate gate = gates.findById(1L).orElseGet(MailGate::new);
        return new Status(gate.isPaused(), Objects.toString(gate.getReason(), ""), dailyUsage(clock.instant()));
    }

    private DailyUsage dailyUsage(Instant now) {
        Instant day = now.truncatedTo(ChronoUnit.DAYS);
        long used = attempts.countByAttemptedAtGreaterThanEqual(day);
        return new DailyUsage(used, Math.max(0, settings.getDailyLimit() - used), day.plus(1, ChronoUnit.DAYS));
    }
}
