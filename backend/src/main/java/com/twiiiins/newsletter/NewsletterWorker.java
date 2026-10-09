package com.twiiiins.newsletter;

import com.twiiiins.security.Tokens;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.data.domain.PageRequest;
import java.time.*;
import java.util.*;

@Component @EnableScheduling @RequiredArgsConstructor @Slf4j
public class NewsletterWorker implements ApplicationRunner {
    private final NewsletterSettings settings;
    private final ObjectProvider<JavaMailSender> senders;
    private final DeliveryRepository deliveries;
    private final MailingRepository mailings;
    private final SubscriberRepository subscribers;
    private final MailAttemptRepository attempts;
    private final MailGateRepository gates;
    private final com.twiiiins.security.AdminSessionRepository sessions;
    private final SubscriptionService subscriptions;
    private final NewsletterContent content;
    private final TransactionTemplate transactions;

    @Override public void run(ApplicationArguments args) {
        transactions.executeWithoutResult(tx -> {
            if (!gates.existsById(1L)) gates.saveAndFlush(new MailGate());
            // One application instance owns the queue. Never resend a message interrupted during SMTP.
            for (Delivery delivery : deliveries.findByStatus("SENDING")) {
                delivery.setStatus("UNKNOWN"); delivery.setErrorCode("RESTART_DURING_SMTP"); delivery.setFinishedAt(Instant.now());
            }
        });
    }
    public record Envelope(Long id, String email, String title, String html, String unsubscribe) {}

    public Envelope claim() {
        return transactions.execute(tx -> {
            if (!settings.ready() || gates.lock().isPaused()) return null;
            Instant now = Instant.now();
            Instant day = now.atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
            if (attempts.countByAttemptedAtGreaterThanEqual(now.minusSeconds(60)) >= settings.getPerMinute()
                || attempts.countByAttemptedAtGreaterThanEqual(day) >= settings.getDailyLimit()) return null;
            var candidates = deliveries.next(now, PageRequest.of(0, 1));
            if (candidates.isEmpty()) return null;
            Delivery delivery = candidates.get(0);
            Subscriber subscriber = delivery.getSubscriberId() == null ? null : subscribers.lockById(delivery.getSubscriberId()).orElse(null);
            String title;
            String html;
            String unsubscribe = null;
            if ("WELCOME".equals(delivery.getKind())) {
                if (subscriber == null || !"ACTIVE".equals(subscriber.getStatus()) || !Objects.equals(delivery.getSubscriptionVersion(), subscriber.getUnsubscribeVersion())) { skip(delivery); return null; }
                boolean de = "de".equals(delivery.getLanguage());
                title = de ? "Willkommen beim TWIIIINS-Newsletter" : "Welcome to the TWIIIINS newsletter";
                unsubscribe = settings.getPublicUrl() + "/newsletter/unsubscribe?token=" + subscriptions.unsubscribeToken(subscriber) + "&lang=" + delivery.getLanguage();
                html = "<h1>" + (de ? "Vielen Dank für Ihr Abonnement!" : "Thank you for subscribing!") + "</h1><p>"
                    + (de ? "Sie erhalten ab jetzt Neuigkeiten über Musik, Auftritte und Veranstaltungen von TWIIIINS. Sie müssen nichts weiter bestätigen." : "You’re subscribed! We’ll send you news about TWIIIINS music, performances and events. No further confirmation is needed.") + "</p>";
                html = content.complete(html, delivery.getLanguage(), settings, null, unsubscribe, false);
            } else if ("CONFIRM".equals(delivery.getKind())) {
                if (subscriber == null || subscriber.getConfirmationExpires() == null || !subscriber.getConfirmationExpires().isAfter(now)
                    || !Objects.equals(delivery.getConfirmationContext(), subscriber.getConfirmationContext()) || "EXCLUDED".equals(subscriber.getStatus())) {
                    skip(delivery); return null;
                }
                boolean de = "de".equals(delivery.getLanguage());
                title = de ? "TWIIIINS – Abonnement bestätigen" : "TWIIIINS – Confirm your subscription";
                String token = Tokens.sign(delivery.getConfirmationContext(), settings.getTokenKey());
                String href = settings.getPublicUrl() + "/newsletter/confirm?token=" + token + "&lang=" + delivery.getLanguage();
                html = "<p>" + (de ? "Bitte bestätigen Sie Ihr Newsletter-Abonnement oder die Sprachänderung." : "Please confirm your newsletter subscription or language change.")
                    + "</p><p><a href=\"" + content.escape(href) + "\">" + (de ? "Bestätigen" : "Confirm subscription") + "</a></p><p>"
                    + (de ? "Der Link ist 24 Stunden gültig. Falls Sie dies nicht angefordert haben, ignorieren Sie diese E-Mail." : "This link expires in 24 hours. If you did not request this, ignore this email.") + "</p>";
            } else if ("NEWS".equals(delivery.getKind())) {
                if (subscriber == null || !"ACTIVE".equals(subscriber.getStatus()) || !Objects.equals(delivery.getSubscriptionVersion(), subscriber.getUnsubscribeVersion())) { skip(delivery); return null; }
                Mailing mailing = mailings.findById(delivery.getMailingId()).orElseThrow();
                boolean de = "de".equals(delivery.getLanguage());
                title = de ? mailing.getTitleDe() : mailing.getTitleEn();
                html = de ? mailing.getHtmlDe() : mailing.getHtmlEn();
                String view = settings.getPublicUrl() + "/media?section=news&newsId=" + mailing.getNewsId() + "&lang=" + delivery.getLanguage();
                unsubscribe = settings.getPublicUrl() + "/newsletter/unsubscribe?token=" + subscriptions.unsubscribeToken(subscriber) + "&lang=" + delivery.getLanguage();
                html = content.complete(html, delivery.getLanguage(), settings, view, unsubscribe, false);
            } else { title = delivery.getTestTitle(); html = delivery.getTestHtml(); }
            if ("TEST".equals(delivery.getKind())) html = content.complete(html, delivery.getLanguage(), settings, null, null, true);
            if ("CONFIRM".equals(delivery.getKind())) html = "<!doctype html><html><body><div style=\"max-width:640px;margin:auto;font-family:Arial,sans-serif;line-height:1.6\">" + html
                + "<hr><p>" + content.escape(settings.getOperator()) + "<br>" + content.escape(settings.getReplyTo()) + "</p></div></body></html>";
            delivery.setStatus("SENDING"); delivery.setAttemptedAt(now); delivery.setAttempts(delivery.getAttempts() + 1);
            MailAttempt attempt = new MailAttempt(); attempt.setDeliveryId(delivery.getId()); attempt.setAttemptedAt(now); attempts.save(attempt);
            return new Envelope(delivery.getId(), delivery.getEmail(), title, html, unsubscribe);
        });
    }
    private void skip(Delivery delivery) { delivery.setStatus("SKIPPED"); delivery.setFinishedAt(Instant.now()); }

    @Scheduled(initialDelayString = "${app.newsletter.worker-initial-delay:30000}", fixedDelay = 1000)
    public void tick() {
        if (!settings.ready()) return;
        Envelope envelope = claim();
        if (envelope == null) return;
        String result = "ACCEPTED";
        String errorCode = null;
        try {
            JavaMailSender sender = senders.getObject();
            var message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(settings.getFrom()); helper.setReplyTo(settings.getReplyTo());
            helper.setTo(envelope.email()); helper.setSubject(envelope.title());
            var alternative = new jakarta.mail.internet.MimeMultipart("alternative");
            var plain = new jakarta.mail.internet.MimeBodyPart();
            plain.setText(content.text(envelope.html()), "UTF-8");
            var html = new jakarta.mail.internet.MimeBodyPart();
            html.setContent(envelope.html(), "text/html; charset=UTF-8");
            alternative.addBodyPart(plain); alternative.addBodyPart(html);
            message.setContent(alternative);
            if (envelope.unsubscribe() != null) message.setHeader("List-Unsubscribe", "<" + envelope.unsubscribe() + ">");
            sender.send(message);
        } catch (Exception e) {
            if (hasCause(e, MailAuthenticationException.class) || smtpCode(e) == 535) { result = "AUTH"; errorCode = "SMTP_AUTH"; }
            else if (smtpCode(e) >= 400 && smtpCode(e) < 500) { result = "TRANSIENT"; errorCode = "SMTP_" + smtpCode(e); }
            else if (smtpCode(e) >= 500) { result = "FAILED"; errorCode = "SMTP_" + smtpCode(e); }
            else if (hasCause(e, org.eclipse.angus.mail.util.MailConnectException.class)) { result = "TRANSIENT"; errorCode = "SMTP_CONNECT"; }
            else if (hasCause(e, MailParseException.class) || hasCause(e, MailPreparationException.class)) { result = "FAILED"; errorCode = "MAIL_FORMAT"; }
            else { result = "UNKNOWN"; errorCode = "SMTP_RESULT_UNKNOWN"; }
            log.warn("Newsletter delivery {}: {}", envelope.id(), errorCode); // Never log recipient addresses or token links.
        }
        finish(envelope.id(), result, errorCode);
    }
    public void finish(Long id, String result, String errorCode) {
        transactions.executeWithoutResult(tx -> {
            MailGate gate = gates.lock();
            Delivery delivery = deliveries.findById(id).orElseThrow();
            delivery.setErrorCode(errorCode);
            if ("AUTH".equals(result)) {
                gate.setPaused(true); gate.setReason("SMTP_AUTH"); delivery.setStatus("WAITING");
            } else if ("TRANSIENT".equals(result) && delivery.getAttempts() <= 3) {
                delivery.setStatus("WAITING"); delivery.setNextAttempt(Instant.now().plusSeconds(60L * (long) Math.pow(5, delivery.getAttempts() - 1)));
            } else {
                delivery.setStatus("TRANSIENT".equals(result) ? "FAILED" : result); delivery.setFinishedAt(Instant.now());
            }
        });
    }
    private boolean hasCause(Throwable e, Class<?> type) {
        if (type.isInstance(e)) return true;
        if (e instanceof MailSendException mail) for (Exception failed : mail.getFailedMessages().values()) if (hasCause(failed, type)) return true;
        return e.getCause() != null && e.getCause() != e && hasCause(e.getCause(), type);
    }
    private int smtpCode(Throwable e) {
        if (e instanceof org.eclipse.angus.mail.smtp.SMTPSendFailedException smtp) return smtp.getReturnCode();
        if (e instanceof org.eclipse.angus.mail.smtp.SMTPAddressFailedException smtp) return smtp.getReturnCode();
        if (e instanceof MailSendException mail) for (Exception failed : mail.getFailedMessages().values()) { int code = smtpCode(failed); if (code != 0) return code; }
        if (e instanceof MessagingException mail && mail.getNextException() != null) { int code = smtpCode(mail.getNextException()); if (code != 0) return code; }
        return e.getCause() != null && e.getCause() != e ? smtpCode(e.getCause()) : 0;
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 3600000)
    public void cleanup() {
        transactions.executeWithoutResult(tx -> {
            Instant now = Instant.now();
            gates.lock();
            for (Subscriber subscriber : subscribers.findAll()) {
                boolean pendingExpired = "PENDING".equals(subscriber.getStatus()) && subscriber.getUpdatedAt().isBefore(now.minusSeconds(7 * 86400));
                boolean removed = Set.of("UNSUBSCRIBED", "EXCLUDED").contains(subscriber.getStatus()) && subscriber.getUpdatedAt().isBefore(now.minusSeconds(30 * 86400));
                if (pendingExpired || removed) {
                    for (Delivery delivery : deliveries.findAll()) {
                        if (Objects.equals(delivery.getSubscriberId(), subscriber.getId())) {
                            if ("CONFIRM".equals(delivery.getKind())) deliveries.delete(delivery);
                            else if ("WAITING".equals(delivery.getStatus())) skip(delivery);
                        }
                    }
                    subscribers.delete(subscriber);
                } else if (subscriber.getConfirmationExpires() != null && subscriber.getConfirmationExpires().isBefore(now)) {
                    subscriber.setConfirmationContext(null); subscriber.setConfirmationHash(null); subscriber.setConfirmationExpires(null); subscriber.setPendingLanguage(null);
                }
            }
            for (Delivery delivery : deliveries.findAll()) {
                if (delivery.getFinishedAt() != null && delivery.getFinishedAt().isBefore(now.minusSeconds(90 * 86400)) && !Set.of("WAITING", "SENDING").contains(delivery.getStatus())) {
                    if (delivery.getMailingId() != null) {
                        Mailing mailing = mailings.findById(delivery.getMailingId()).orElseThrow();
                        switch (delivery.getStatus()) {
                            case "ACCEPTED" -> mailing.setAcceptedCount(mailing.getAcceptedCount() + 1);
                            case "FAILED" -> mailing.setFailedCount(mailing.getFailedCount() + 1);
                            case "UNKNOWN" -> mailing.setUnknownCount(mailing.getUnknownCount() + 1);
                            case "SKIPPED" -> mailing.setSkippedCount(mailing.getSkippedCount() + 1);
                        }
                    }
                    deliveries.delete(delivery);
                }
            }
            attempts.deleteByAttemptedAtBefore(now.minusSeconds(90 * 86400));
            sessions.findAll().stream().filter(s -> s.getExpiresAt().isBefore(now)).forEach(sessions::delete);
        });
    }
}
