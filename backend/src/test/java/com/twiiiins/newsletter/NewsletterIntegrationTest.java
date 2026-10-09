package com.twiiiins.newsletter;

import com.twiiiins.dto.request.*;
import com.twiiiins.dto.NewsDto;
import com.twiiiins.entity.User;
import com.twiiiins.repository.*;
import com.twiiiins.service.NewsService;
import com.twiiiins.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:newsletter;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop", "app.newsletter.worker-initial-delay=86400000", "app.newsletter.enabled=false"
})
@AutoConfigureMockMvc
class NewsletterIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired NewsService news;
    @Autowired NewsRepository newsRepository;
    @Autowired SubscriptionService subscriptions;
    @Autowired SubscriberRepository subscribers;
    @Autowired MailingService mailingService;
    @Autowired MailingRepository mailings;
    @Autowired DeliveryRepository deliveries;
    @Autowired MailAttemptRepository attempts;
    @Autowired NewsletterWorker worker;
    @Autowired NewsletterSettings settings;
    @Autowired MailGateRepository gates;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired AdminSessionRepository sessions;
    @Autowired NewsletterContent content;
    @MockBean JavaMailSender sender;

    @BeforeEach void setup() {
        deliveries.deleteAll(); attempts.deleteAll(); mailings.deleteAll(); subscribers.deleteAll(); newsRepository.deleteAll(); sessions.deleteAll();
        settings.setEnabled(true); settings.setSmtpHost("localhost"); settings.setTokenKey("test-key-for-local-tests-only-123456789");
        settings.setFrom("news@example.com"); settings.setReplyTo("reply@example.com"); settings.setOperator("TWIIIINS Test Operator");
        settings.setPublicUrl("https://example.com"); settings.setPerMinute(10); settings.setDailyLimit(300); settings.setTestRecipients(List.of("admin@example.com"));
        MailGate gate = gates.findById(1L).orElseGet(MailGate::new); gate.setPaused(false); gate.setReason(null); gates.save(gate);
        when(sender.createMimeMessage()).thenAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
    }
    @Test @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void draftImageUploadDoesNotRequireOrSaveANewsDraft() throws Exception {
        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jfsUAAAAASUVORK5CYII=");
        var image = new org.springframework.mock.web.MockMultipartFile("files", "test.png", "image/png", png);
        mvc.perform(multipart("/api/admin/newsletter/images").file(image))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
        assertEquals(0, newsRepository.count()); assertEquals(0, deliveries.count());
    }
    @Test void newsletterImageUploadRequiresAuthentication() throws Exception {
        var image = new org.springframework.mock.web.MockMultipartFile("files", "test.png", "image/png", new byte[]{1});
        mvc.perform(multipart("/api/admin/newsletter/images").file(image)).andExpect(status().isUnauthorized());
    }
    @Test void subscriptionActivatesImmediatelyQueuesOneWelcomeAndRecordsConsent() throws Exception {
        mvc.perform(post("/api/newsletter/subscribe").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"instant@example.com\",\"language\":\"de\",\"consent\":false}"))
            .andExpect(status().isBadRequest());
        assertEquals(0, subscribers.count());
        mvc.perform(post("/api/newsletter/subscribe").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"instant@example.com\",\"language\":\"de\",\"consent\":true}"))
            .andExpect(status().isOk());
        Subscriber subscriber = subscribers.findByEmail("instant@example.com").orElseThrow();
        assertEquals("ACTIVE", subscriber.getStatus()); assertEquals("de", subscriber.getLanguage());
        assertNotNull(subscriber.getConsentAt()); assertEquals("newsletter-v2-single-opt-in", subscriber.getConsentVersion());
        assertNull(subscriber.getConfirmationContext()); assertNull(subscriber.getConfirmationHash()); assertNull(subscriber.getConfirmationExpires());
        assertEquals("WELCOME", deliveries.findAll().get(0).getKind());
        subscriptions.subscribe(" INSTANT@example.com ", "de");
        assertEquals(1, subscribers.count()); assertEquals(1, deliveries.count());
        NewsletterWorker.Envelope welcome = worker.claim();
        assertTrue(welcome.title().contains("Willkommen")); assertTrue(welcome.html().contains("Vielen Dank"));
        assertTrue(welcome.html().contains("/newsletter/unsubscribe?token=")); assertFalse(welcome.html().contains("/newsletter/confirm"));
        assertNotNull(welcome.unsubscribe());
        assertFalse(welcome.html().contains("(preview)")); assertFalse(welcome.html().contains("(Vorschau)"));
        worker.finish(welcome.id(), "FAILED", "SMTP_TEMPORARY");
        assertEquals("ACTIVE", subscribers.findById(subscriber.getId()).orElseThrow().getStatus());
    }
    @Test void legacyPendingSignupCanSubscribeImmediatelyAndInvalidatesOldConfirmation() {
        Subscriber pending = legacyPending("pending@example.com", "de");
        String oldToken = Tokens.sign(pending.getConfirmationContext(), settings.getTokenKey());
        subscriptions.subscribe(pending.getEmail(), "de");
        Subscriber current = subscribers.findById(pending.getId()).orElseThrow();
        assertEquals("ACTIVE", current.getStatus()); assertNull(current.getConfirmationContext());
        assertThrows(IllegalArgumentException.class, () -> subscriptions.confirm(oldToken));
        assertEquals("WELCOME", deliveries.findAll().get(0).getKind());
    }
    @Test void welcomeEmailsUseTheChosenLanguageAndAWorkingPersonalUnsubscribeLink() {
        for (String language : List.of("en", "de")) {
            Subscriber subscriber = active(language + "@example.com", language);
            NewsletterWorker.Envelope envelope = worker.claim();
            assertNotNull(envelope);
            assertTrue(envelope.html().contains("de".equals(language) ? "Vielen Dank für Ihr Abonnement" : "Thank you for subscribing"));
            assertFalse(envelope.html().contains("/newsletter/confirm"));
            worker.finish(envelope.id(), "ACCEPTED", null);
            String token = java.net.URI.create(envelope.unsubscribe()).getRawQuery().split("&")[0].substring("token=".length());
            subscriptions.unsubscribe(token);
            assertEquals("UNSUBSCRIBED", subscribers.findById(subscriber.getId()).orElseThrow().getStatus());
        }
    }
    @Test void welcomeIsSkippedAfterUnsubscribeAndExcludedAddressesStayExcluded() {
        Subscriber subscriber = active("welcome@example.com", "en");
        subscriptions.unsubscribe(subscriptions.unsubscribeToken(subscriber));
        assertNull(worker.claim());
        assertEquals("SKIPPED", deliveries.findAll().get(0).getStatus());
        subscriptions.exclude(subscriber.getId()); subscriptions.subscribe(subscriber.getEmail(), "de");
        assertEquals("EXCLUDED", subscribers.findById(subscriber.getId()).orElseThrow().getStatus());
        assertEquals(1, deliveries.count());
    }
    @Test void localMailboxHintIsHiddenForPublicDeploymentsAndInvalidUrls() {
        settings.setLocalMailboxUrl("http://localhost:8025");
        assertEquals("", settings.localMailboxUrl());
        settings.setPublicUrl("http://localhost:5173");
        assertEquals("http://localhost:8025", settings.localMailboxUrl());
        settings.setLocalMailboxUrl("javascript:alert(1)"); assertEquals("", settings.localMailboxUrl());
        settings.setLocalMailboxUrl("");
    }
    NewsDto draft() {
        NewsCreateRequest request = new NewsCreateRequest(); request.setDate(LocalDate.now()); request.setTitle("English title"); request.setTitleDe("Deutscher Titel");
        request.setBodyEn("<p>Hello <strong>friends</strong></p><script>alert(1)</script><a href='javascript:alert(1)'>bad</a>");
        request.setBodyDe("<h2>Hallo</h2><p>Neuigkeiten</p>"); request.setVideoUrls(List.of("https://example.com/video"));
        return news.createNews(request);
    }
    Subscriber active(String email, String lang) {
        subscriptions.subscribe(email, lang);
        Subscriber subscriber = subscribers.findByEmail(email).orElseThrow();
        return subscribers.findById(subscriber.getId()).orElseThrow();
    }
    Subscriber legacyPending(String email, String language) {
        Subscriber subscriber = new Subscriber(); subscriber.setEmail(email); subscriber.setPendingLanguage(language);
        subscribers.saveAndFlush(subscriber);
        String context = "confirm|" + subscriber.getId() + "|legacy|" + language;
        subscriber.setConfirmationContext(context); subscriber.setConfirmationHash(Tokens.hash(Tokens.sign(context, settings.getTokenKey())));
        subscriber.setConfirmationExpires(Instant.now().plusSeconds(86400));
        return subscribers.saveAndFlush(subscriber);
    }
    void clearNotificationJobs() { deliveries.deleteAll(); }
    void tickQueuedMail() {
        // Exercise SMTP with explicitly due jobs, independent of database timestamp rounding.
        for (Delivery delivery : deliveries.findByStatus("WAITING")) {
            delivery.setNextAttempt(Instant.now().minusSeconds(1)); deliveries.save(delivery);
        }
        worker.tick();
    }

    @Test @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void manualNewsCrudPublishesImmediatelyWithoutMailAndKeepsDescriptionCurrent() throws Exception {
        String response = mvc.perform(post("/api/media/news").contentType(MediaType.APPLICATION_JSON)
            .content("{\"date\":\"2026-10-07\",\"title\":\"Regular news\",\"description\":\"First description\",\"source\":\"NEWSLETTER\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.source").value("NEWS"))
            .andExpect(jsonPath("$.data.status").value("PUBLISHED")).andReturn().getResponse().getContentAsString();
        long id = json.readTree(response).path("data").path("id").asLong();
        mvc.perform(get("/api/media/news").param("title", "Regular").param("startDate", "2026-10-07"))
            .andExpect(jsonPath("$.data.length()").value(1));
        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jfsUAAAAASUVORK5CYII=");
        var image = new org.springframework.mock.web.MockMultipartFile("files", "test.png", "image/png", png);
        mvc.perform(multipart("/api/media/news/" + id + "/images").file(image))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.imageUrls.length()").value(1));
        var item = news.adminGet(id);
        mvc.perform(put("/api/media/news/" + id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"date\":\"2026-10-07\",\"title\":\"Updated regular news\",\"description\":\"New <text>\",\"imageUrls\":[],\"version\":" + item.getVersion() + "}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.source").value("NEWS"))
            .andExpect(jsonPath("$.data.imageUrls.length()").value(0));
        mvc.perform(get("/api/media/news/" + id)).andExpect(jsonPath("$.data.body").value("<p>New &lt;text&gt;</p>"));
        assertEquals(0, deliveries.count()); assertEquals(0, mailings.count());
        mvc.perform(delete("/api/media/news/" + id)).andExpect(status().is2xxSuccessful());
        assertFalse(newsRepository.existsById(id));
    }

    @Test @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void sourceFiltersAndApiGuardsSeparateGeneralNewsFromNewsletters() throws Exception {
        NewsDto newsletter = draft();
        NewsCreateRequest request = new NewsCreateRequest(); request.setDate(LocalDate.now()); request.setTitle("Manual");
        NewsDto manual = news.createManualNews(request);
        assertEquals("NEWSLETTER", newsletter.getSource());
        mvc.perform(get("/api/admin/news").param("source", "NEWSLETTER"))
            .andExpect(jsonPath("$.data.length()").value(1)).andExpect(jsonPath("$.data[0].id").value(newsletter.getId()));
        mvc.perform(get("/api/admin/news").param("source", "NEWS"))
            .andExpect(jsonPath("$.data.length()").value(1)).andExpect(jsonPath("$.data[0].id").value(manual.getId()));
        mvc.perform(get("/api/admin/news").param("source", "invalid")).andExpect(status().isBadRequest());
        String update = "{\"date\":\"2026-10-07\",\"title\":\"Cannot overwrite\"}";
        mvc.perform(put("/api/media/news/" + newsletter.getId()).contentType(MediaType.APPLICATION_JSON).content(update)).andExpect(status().isConflict());
        mvc.perform(delete("/api/media/news/" + newsletter.getId())).andExpect(status().isConflict());
        var image = new org.springframework.mock.web.MockMultipartFile("files", "test.png", "image/png", new byte[]{1});
        mvc.perform(multipart("/api/media/news/" + newsletter.getId() + "/images").file(image)).andExpect(status().isConflict());
        mvc.perform(put("/api/admin/news/" + manual.getId()).contentType(MediaType.APPLICATION_JSON).content(update)).andExpect(status().isConflict());
        mvc.perform(delete("/api/admin/news/" + manual.getId())).andExpect(status().isConflict());
        mvc.perform(post("/api/admin/news/" + manual.getId() + "/publish")).andExpect(status().isConflict());
        mvc.perform(post("/api/admin/news/" + manual.getId() + "/send").contentType(MediaType.APPLICATION_JSON).content("{\"version\":" + manual.getVersion() + "}")).andExpect(status().isConflict());
        mvc.perform(multipart("/api/admin/news/" + manual.getId() + "/images").file(image)).andExpect(status().isConflict());
        assertEquals(0, deliveries.count());
    }

    @Test @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void newsletterPublicationUpdatesTheSamePublicEntryWithoutDuplicatingIt() throws Exception {
        NewsDto item = draft();
        mvc.perform(get("/api/media/news")).andExpect(jsonPath("$.data.length()").value(0));
        for (int i = 0; i < 2; i++) mvc.perform(post("/api/admin/news/" + item.getId() + "/publish")).andExpect(status().isOk());
        mvc.perform(get("/api/media/news")).andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].source").value("NEWSLETTER")).andExpect(jsonPath("$.data[0].id").value(item.getId()));
        NewsUpdateRequest update = new NewsUpdateRequest(); update.setBodyEn("<p>Changed online</p>"); news.updateNews(item.getId(), update);
        mvc.perform(get("/api/media/news/" + item.getId())).andExpect(jsonPath("$.data.body").value("<p>Changed online</p>"));
        assertEquals(1, newsRepository.count()); assertEquals(0, mailings.count());
    }

    @Test @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void subscriberListShowsChosenLanguageDatesAndFiltersWithoutInternalTokens() throws Exception {
        active("active@example.com", "en");
        legacyPending("pending@example.com", "de");
        mvc.perform(get("/api/admin/newsletter/subscribers"))
            .andExpect(jsonPath("$.data.length()").value(2)).andExpect(jsonPath("$.data[0].createdAt").isNotEmpty())
            .andExpect(jsonPath("$.data[0].confirmationContext").doesNotExist())
            .andExpect(jsonPath("$.data[0].confirmationExpires").doesNotExist())
            .andExpect(jsonPath("$.data[0].unsubscribeVersion").doesNotExist());
        mvc.perform(get("/api/admin/newsletter/subscribers").param("language", "de").param("status", "PENDING").param("search", " PENDING@ "))
            .andExpect(jsonPath("$.data.length()").value(1)).andExpect(jsonPath("$.data[0].email").value("pending@example.com"));
        var excluded = subscribers.findByEmail("active@example.com").orElseThrow(); subscriptions.exclude(excluded.getId());
        mvc.perform(get("/api/admin/newsletter/subscribers").param("status", "EXCLUDED"))
            .andExpect(jsonPath("$.data.length()").value(1));
        assertTrue(subscribers.findByStatus("ACTIVE").isEmpty());
    }

    @Test void serverAuthProtectsReadsWritesAndRevokesSessions() throws Exception {
        mvc.perform(get("/api/admin/newsletter/subscribers")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/media/news").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
        User user = users.findByUsername("newsletter-test-admin").orElseGet(User::new);
        user.setUsername("newsletter-test-admin"); user.setPassword(passwords.encode("test-only-password")); users.save(user);
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"newsletter-test-admin\",\"password\":\"test-only-password\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = json.readTree(response).path("data").path("token").asText();
        assertFalse(token.isBlank()); assertTrue(sessions.existsById(Tokens.hash(token))); assertFalse(sessions.existsById(token));
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/api/admin/news").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        var session = sessions.findById(Tokens.hash(token)).orElseThrow(); session.setExpiresAt(Instant.now().minusSeconds(1)); sessions.save(session);
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        session.setExpiresAt(Instant.now().plusSeconds(60)); sessions.save(session);
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
    @Test void draftIsPrivateAndHtmlIsSanitized() throws Exception {
        NewsDto item = draft();
        assertFalse(item.getBodyEn().contains("<script")); assertFalse(item.getBodyEn().contains("javascript:")); assertTrue(item.getBodyEn().contains("<strong>"));
        assertTrue(news.getAllNews().isEmpty());
        mvc.perform(get("/api/media/news/" + item.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/media/news").param("title", "English")).andExpect(jsonPath("$.data.length()").value(0));
        news.publish(item.getId()); assertEquals(1, news.getAllNews().size());
        assertThrows(IllegalArgumentException.class, () -> content.url("//evil.example/image", settings.getPublicUrl()));
        assertThrows(IllegalArgumentException.class, () -> content.url("javascript:alert(1)", settings.getPublicUrl()));
    }
    @Test void languageChangesImmediatelyAndLegacyConfirmationLinksStillExpire() {
        Subscriber subscriber = active("person@example.com", "en");
        String originalUnsubscribe = subscriptions.unsubscribeToken(subscriber);
        subscriptions.subscribe(subscriber.getEmail(), "de");
        assertEquals("de", subscribers.findById(subscriber.getId()).orElseThrow().getLanguage());
        assertEquals(1, deliveries.count()); // do not repeatedly send welcome mail for rapid language changes
        subscriptions.unsubscribe(originalUnsubscribe);
        assertEquals("UNSUBSCRIBED", subscribers.findById(subscriber.getId()).orElseThrow().getStatus());
        Subscriber pending = legacyPending("legacy@example.com", "de");
        String token = Tokens.sign(pending.getConfirmationContext(), settings.getTokenKey());
        pending.setConfirmationExpires(Instant.now().minusSeconds(1)); subscribers.save(pending);
        assertThrows(IllegalArgumentException.class, () -> subscriptions.confirm(token));
        pending.setConfirmationExpires(Instant.now().plusSeconds(60)); subscribers.save(pending); subscriptions.confirm(token);
        assertEquals("ACTIVE", subscribers.findById(pending.getId()).orElseThrow().getStatus());
        assertThrows(IllegalArgumentException.class, () -> subscriptions.confirm(token));
        assertThrows(IllegalArgumentException.class, () -> subscriptions.confirm(token + "x"));
    }
    @Test void idempotentSnapshotUsesRecipientLanguageAndSkipsUnsubscribed() {
        Subscriber en = active("english@example.com", "en"), de = active("deutsch@example.com", "de"); clearNotificationJobs();
        NewsDto item = draft();
        Mailing mailing = mailingService.send(item.getId(), item.getVersion());
        assertEquals(mailing.getId(), mailingService.send(item.getId(), item.getVersion()).getId());
        assertEquals(2, deliveries.findByMailingIdOrderByIdAsc(mailing.getId()).size());
        NewsUpdateRequest update = new NewsUpdateRequest(); update.setTitle("Changed title"); update.setBodyEn("<p>New content</p>"); news.updateNews(item.getId(), update);
        assertEquals("English title", mailings.findById(mailing.getId()).orElseThrow().getTitleEn());
        subscriptions.unsubscribe(subscriptions.unsubscribeToken(en));
        assertNull(worker.claim());
        NewsletterWorker.Envelope envelope = worker.claim();
        assertEquals(de.getEmail(), envelope.email()); assertEquals("Deutscher Titel", envelope.title()); assertTrue(envelope.html().contains("Hallo"));
        assertTrue(envelope.html().contains("newsId=" + item.getId())); assertTrue(envelope.html().contains("Unsub") || envelope.html().contains("Abmelden"));
        worker.finish(envelope.id(), "ACCEPTED", null);
        news.deleteNews(item.getId()); assertTrue(news.adminGet(item.getId()).isArchived()); assertNotNull(news.getNewsById(item.getId()));
    }
    @Test void resubscribingDoesNotReceiveAnOldQueuedMailing() {
        Subscriber subscriber = active("again@example.com", "en"); clearNotificationJobs();
        NewsDto item = draft(); Mailing mailing = mailingService.send(item.getId(), item.getVersion());
        subscriptions.unsubscribe(subscriptions.unsubscribeToken(subscriber));
        subscriber = subscribers.findById(subscriber.getId()).orElseThrow(); subscriber.setRequestedAt(Instant.now().minusSeconds(601)); subscribers.save(subscriber);
        active(subscriber.getEmail(), "en");
        assertNull(worker.claim()); // old subscription version is skipped
        assertNotNull(worker.claim()); // the new subscription receives its welcome email
        assertEquals("SKIPPED", deliveries.findByMailingIdOrderByIdAsc(mailing.getId()).get(0).getStatus());
    }
    @Test void quotaAndRestartNeverAutomaticallyResendAmbiguousMessages() {
        active("one@example.com", "en"); active("two@example.com", "de"); clearNotificationJobs();
        NewsDto item = draft(); mailingService.send(item.getId(), item.getVersion());
        settings.setPerMinute(1);
        NewsletterWorker.Envelope first = worker.claim(); assertNotNull(first); assertNull(worker.claim());
        worker.run(null);
        assertEquals("UNKNOWN", deliveries.findById(first.id()).orElseThrow().getStatus());
        assertThrows(IllegalArgumentException.class, () -> mailingService.retry(first.id()));
        settings.setPerMinute(10); settings.setDailyLimit(1); assertNull(worker.claim());
        settings.setDailyLimit(300); assertNotNull(worker.claim());
    }
    @Test void smtpMessageContainsPlainAndHtmlAndSingleRecipient() throws Exception {
        Subscriber subscriber = active("mime@example.com", "de"); clearNotificationJobs();
        NewsDto item = draft(); mailingService.send(item.getId(), item.getVersion()); tickQueuedMail();
        var captor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class); verify(sender).send(captor.capture());
        MimeMessage message = captor.getValue();
        message.saveChanges();
        assertEquals(1, message.getAllRecipients().length); assertEquals(subscriber.getEmail(), message.getAllRecipients()[0].toString());
        assertEquals("Deutscher Titel", message.getSubject()); assertNotNull(message.getHeader("List-Unsubscribe"));
        assertTrue(message.getContentType().startsWith("multipart/alternative"));
        assertEquals("ACCEPTED", deliveries.findAll().get(0).getStatus());
    }
    @Test void authFailurePausesQueueAndSafeFailureCanRetry() {
        active("errors@example.com", "en"); clearNotificationJobs();
        NewsDto item = draft(); mailingService.send(item.getId(), item.getVersion());
        doThrow(new org.springframework.mail.MailAuthenticationException("test")).when(sender).send(any(MimeMessage.class)); tickQueuedMail();
        assertTrue(gates.findById(1L).orElseThrow().isPaused()); assertEquals("WAITING", deliveries.findAll().get(0).getStatus());
        mailingService.resume(); reset(sender); when(sender.createMimeMessage()).thenAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
        NewsletterWorker.Envelope delivery = worker.claim(); worker.finish(delivery.id(), "FAILED", "SMTP_550");
        mailingService.retry(delivery.id()); assertEquals("WAITING", deliveries.findById(delivery.id()).orElseThrow().getStatus());
        Delivery due = deliveries.findById(delivery.id()).orElseThrow(); due.setNextAttempt(Instant.now().minusSeconds(1)); deliveries.save(due);
        NewsletterWorker.Envelope retry = worker.claim(); assertNotNull(retry); worker.finish(retry.id(), "TRANSIENT", "SMTP_450");
        Delivery waiting = deliveries.findById(retry.id()).orElseThrow(); assertEquals("WAITING", waiting.getStatus()); assertTrue(waiting.getNextAttempt().isAfter(Instant.now()));
    }
    @Test void threeHundredRecipientsAreProcessedWithinQuotaWithoutDuplicates() {
        List<Subscriber> batch = new ArrayList<>();
        for (int i = 0; i < 300; i++) { Subscriber s = new Subscriber(); s.setEmail("load" + i + "@example.com"); s.setStatus("ACTIVE"); s.setLanguage(i % 2 == 0 ? "en" : "de"); batch.add(s); }
        subscribers.saveAll(batch); settings.setPerMinute(500);
        NewsDto item = draft(); Mailing mailing = mailingService.send(item.getId(), item.getVersion()); Set<Long> seen = new HashSet<>();
        for (int i = 0; i < 300; i++) { NewsletterWorker.Envelope envelope = worker.claim(); assertNotNull(envelope); assertTrue(seen.add(envelope.id())); worker.finish(envelope.id(), "ACCEPTED", null); }
        assertNull(worker.claim()); assertEquals(300, attempts.count());
        assertEquals(300, deliveries.findByMailingIdOrderByIdAsc(mailing.getId()).stream().filter(d -> "ACCEPTED".equals(d.getStatus())).count());
    }
    @Test void disabledSmtpRejectsSubscriptionButAllowsNewsDrafts() throws Exception {
        settings.setEnabled(false);
        mvc.perform(post("/api/newsletter/subscribe").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"disabled@example.com\",\"language\":\"en\",\"consent\":true}"))
            .andExpect(status().isServiceUnavailable());
        assertTrue(subscribers.findAll().isEmpty()); assertNotNull(draft());
    }
    @Test void retentionRemovesExpiredPersonalDataAndPreservesMailingCounts() {
        Subscriber pending = new Subscriber(); pending.setEmail("expired-pending@example.com"); pending.setUpdatedAt(Instant.now().minusSeconds(8 * 86400)); subscribers.save(pending);
        Subscriber removed = new Subscriber(); removed.setEmail("removed@example.com"); removed.setStatus("UNSUBSCRIBED"); removed.setUpdatedAt(Instant.now().minusSeconds(31 * 86400)); subscribers.save(removed);
        active("retain-active@example.com", "en"); clearNotificationJobs();
        NewsDto item = draft(); Mailing mailing = mailingService.send(item.getId(), item.getVersion());
        NewsletterWorker.Envelope envelope = worker.claim(); worker.finish(envelope.id(), "ACCEPTED", null);
        Delivery delivery = deliveries.findById(envelope.id()).orElseThrow(); delivery.setFinishedAt(Instant.now().minusSeconds(91 * 86400)); deliveries.save(delivery);
        worker.cleanup();
        assertTrue(subscribers.findByEmail(pending.getEmail()).isEmpty()); assertTrue(subscribers.findByEmail(removed.getEmail()).isEmpty());
        assertTrue(subscribers.findByEmail("retain-active@example.com").isPresent()); assertFalse(deliveries.existsById(envelope.id()));
        assertEquals(1, mailings.findById(mailing.getId()).orElseThrow().getAcceptedCount());
        worker.cleanup(); assertEquals(1, mailings.findById(mailing.getId()).orElseThrow().getAcceptedCount());
    }
    @Test void actualSmtpTransportDeliversMimeToLocalServerOnly() throws Exception {
        try (var server = new java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress())) {
            server.setSoTimeout(10000);
            var received = new java.util.concurrent.CompletableFuture<String>();
            Thread smtp = new Thread(() -> {
                try (var socket = server.accept()) {
                    socket.setSoTimeout(10000);
                    var input = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                    var output = new java.io.PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
                    output.print("220 localhost test SMTP\r\n"); output.flush();
                    String line;
                    while ((line = input.readLine()) != null) {
                        if (line.startsWith("EHLO")) output.print("250-localhost\r\n250 8BITMIME\r\n");
                        else if (line.equals("DATA")) {
                            output.print("354 Send message\r\n"); output.flush(); var data = new StringBuilder();
                            while ((line = input.readLine()) != null && !line.equals(".")) data.append(line).append("\r\n");
                            received.complete(data.toString()); output.print("250 Queued\r\n");
                        } else if (line.equals("QUIT")) { output.print("221 Bye\r\n"); output.flush(); break; }
                        else output.print("250 OK\r\n");
                        output.flush();
                    }
                } catch (Exception e) { received.completeExceptionally(e); }
            }, "local-test-smtp");
            smtp.setDaemon(true); smtp.start();
            var realSender = new org.springframework.mail.javamail.JavaMailSenderImpl(); realSender.setHost(server.getInetAddress().getHostAddress()); realSender.setPort(server.getLocalPort());
            realSender.getJavaMailProperties().put("mail.smtp.connectiontimeout", "10000"); realSender.getJavaMailProperties().put("mail.smtp.timeout", "10000");
            doAnswer(invocation -> { realSender.send((MimeMessage) invocation.getArgument(0)); return null; }).when(sender).send(any(MimeMessage.class));
            active("local-delivery@example.com", "de"); clearNotificationJobs();
            NewsDto item = draft(); mailingService.send(item.getId(), item.getVersion()); tickQueuedMail();
            String raw = received.get(10, java.util.concurrent.TimeUnit.SECONDS);
            var mime = new MimeMessage(Session.getInstance(new Properties()), new java.io.ByteArrayInputStream(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            assertEquals("Deutscher Titel", mime.getSubject()); assertEquals(1, mime.getAllRecipients().length);
            var parts = (jakarta.mail.Multipart) mime.getContent(); assertEquals(2, parts.getCount());
            assertTrue(parts.getBodyPart(0).getContent().toString().contains("/newsletter/unsubscribe?token="));
            assertTrue(parts.getBodyPart(1).getContent().toString().contains("<h2>Hallo</h2>"));
            assertEquals("ACCEPTED", deliveries.findAll().get(0).getStatus()); smtp.join(1000);
        }
    }

    @Test void unsavedPreviewRequiresAdministrator() throws Exception {
        mvc.perform(post("/api/admin/newsletter/preview").contentType(MediaType.APPLICATION_JSON).content("{\"language\":\"en\"}"))
            .andExpect(status().isUnauthorized());
    }
    @Test @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void unsavedPreviewAcceptsIncompleteDraftWithoutSmtpOrDatabaseWrites() throws Exception {
        settings.setEnabled(false); settings.setOperator(""); settings.setReplyTo("");
        long before = newsRepository.count();
        String response = mvc.perform(post("/api/admin/newsletter/preview").contentType(MediaType.APPLICATION_JSON)
            .content("{\"language\":\"en\",\"title\":\"Live <show>\",\"bodyEn\":\"<p>Draft</p><script>alert(1)</script>\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String html = json.readTree(response).path("data").path("html").asText();
        assertTrue(html.startsWith("<!doctype html>")); assertTrue(html.contains("#80ffcf")); assertTrue(html.contains("#8300ef"));
        assertTrue(html.contains("Live &lt;show&gt;")); assertTrue(html.contains("Example: sender information"));
        assertTrue(html.contains("Unsubscribe (preview)")); assertFalse(html.contains("<script")); assertFalse(html.contains("token="));
        assertEquals(before, newsRepository.count()); assertEquals(0, deliveries.count());
        mvc.perform(post("/api/admin/newsletter/preview").contentType(MediaType.APPLICATION_JSON).content("{\"language\":\"en\",\"ctaUrl\":\"javascript:alert(1)\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/newsletter/preview").contentType(MediaType.APPLICATION_JSON).content("{\"language\":\"fr\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void newsletterDesignFieldsPersistAndRequireBilingualPublication() {
        NewsCreateRequest request = new NewsCreateRequest(); request.setDate(LocalDate.now()); request.setTitle("Show"); request.setTitleDe("Konzert");
        request.setBodyEn("<p>English body</p>"); request.setBodyDe("<p>Deutscher Text</p>");
        request.setEventWhenEn("Saturday 7pm"); request.setEventLocationEn("Hall"); request.setCtaLabelEn("Tickets"); request.setCtaUrl("https://example.com/tickets");
        request.setImageUrls(List.of("/uploads/hero.jpg", "/uploads/detail.jpg"));
        NewsDto item = news.createNews(request);
        assertEquals("Saturday 7pm", item.getEventWhenEn()); assertEquals("https://example.com/tickets", item.getCtaUrl());
        Long draftId = item.getId();
        assertThrows(IllegalArgumentException.class, () -> news.publish(draftId));
        NewsUpdateRequest update = new NewsUpdateRequest(); update.setEventWhenDe("Samstag 19 Uhr"); update.setEventLocationDe("Saal"); update.setCtaLabelDe("Karten");
        item = news.updateNews(item.getId(), update); news.publish(item.getId());
        String html = (String) mailingService.preview(item.getId(), "de").get("html");
        assertTrue(html.contains("Samstag 19 Uhr")); assertTrue(html.contains("Saal")); assertTrue(html.contains(">Karten</a>"));
        assertTrue(html.indexOf("hero.jpg") < html.indexOf("Samstag 19 Uhr"));
        assertTrue(html.indexOf("Deutscher Text") < html.indexOf("detail.jpg"));
        update = new NewsUpdateRequest(); update.setEventWhenEn(""); update.setEventWhenDe("");
        NewsDto cleared = news.updateNews(item.getId(), update); assertEquals("", cleared.getEventWhenEn()); assertEquals("", cleared.getEventWhenDe());
    }
    @Test void newAndLegacyQueuedSnapshotsKeepTheirBodyAndReceiveRealFooter() {
        active("snapshot@example.com", "en"); clearNotificationJobs();
        NewsDto item = draft(); Mailing mailing = mailingService.send(item.getId(), item.getVersion());
        String snapshot = mailing.getHtmlEn();
        NewsUpdateRequest update = new NewsUpdateRequest(); update.setBodyEn("<p>Changed after queueing</p>"); news.updateNews(item.getId(), update);
        assertEquals(snapshot, mailings.findById(mailing.getId()).orElseThrow().getHtmlEn());
        NewsletterWorker.Envelope envelope = worker.claim(); assertNotNull(envelope);
        assertTrue(envelope.html().contains("Hello")); assertFalse(envelope.html().contains("Changed after queueing"));
        assertTrue(envelope.html().contains("/newsletter/unsubscribe?token=")); assertFalse(envelope.html().contains("<!--NEWSLETTER_FOOTER-->"));
        worker.finish(envelope.id(), "ACCEPTED", null);
        NewsDto legacy = draft(); Mailing old = mailingService.send(legacy.getId(), legacy.getVersion());
        old.setHtmlEn("<h1>Legacy queued body</h1>"); mailings.save(old);
        envelope = worker.claim(); assertNotNull(envelope); assertTrue(envelope.html().contains("<h1>Legacy queued body</h1>"));
        assertTrue(envelope.html().contains("/newsletter/unsubscribe?token="));
    }
}
