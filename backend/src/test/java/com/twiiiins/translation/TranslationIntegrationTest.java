package com.twiiiins.translation;

import com.twiiiins.dto.*;
import com.twiiiins.dto.request.*;
import com.twiiiins.entity.*;
import com.twiiiins.service.*;
import com.twiiiins.repository.*;
import com.twiiiins.newsletter.MailingService;
import com.twiiiins.newsletter.NewsletterContent;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:translations;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop", "app.newsletter.worker-initial-delay=86400000",
    "app.translation.worker-initial-delay=86400000", "app.newsletter.enabled=false"
})
@AutoConfigureMockMvc
class TranslationIntegrationTest {
    @Autowired TranslationRepository rows;
    @Autowired TranslationService translations;
    @Autowired TranslationSettings settings;
    @Autowired TranslationWorker worker;
    @Autowired ProjectService projects;
    @Autowired ProjectRepository projectRepository;
    @Autowired NewsService news;
    @Autowired NewsRepository newsRepository;
    @Autowired TranslationFields fields;
    @Autowired NewsletterContent html;
    @Autowired MockMvc mvc;
    @Autowired PlatformTransactionManager transactions;
    @MockBean GeminiTranslator gemini;

    @BeforeEach void setup() {
        rows.deleteAll(); projectRepository.deleteAll(); newsRepository.deleteAll();
        settings.setEnabled(true); settings.setApiKey("test-key-never-sent");
    }
    ProjectDto project(String text) {
        ProjectCreateRequest r = new ProjectCreateRequest(); r.setTitle(text); r.setUrlSlug("translation-test");
        return projects.createProject(r);
    }
    ContentTranslation row(String kind, Long id, String path) {
        return rows.findByKindAndContentId(kind, id).stream().filter(r -> path.equals(r.getFieldPath())).findFirst().orElseThrow();
    }
    ContentTranslation row(ProjectDto p) { return row("projects", p.getId(), "title"); }
    void complete(ContentTranslation r, String language, String en, String de) {
        when(gemini.translate(any())).thenReturn(new GeminiTranslator.Result(language, en, de)); worker.process(r.getId());
    }
    void change(ProjectDto p, String text) {
        ProjectUpdateRequest r = new ProjectUpdateRequest(); r.setTitle(text); projects.updateProject(p.getId(), r);
    }
    NewsDto draft(String en, String de) {
        NewsCreateRequest r = new NewsCreateRequest(); r.setDate(LocalDate.of(2026, 10, 30));
        r.setTitle(en); r.setTitleDe(de);
        if (en != null) r.setBodyEn("<p>Hello TWIIIINS</p>");
        if (de != null) r.setBodyDe("<p>Hallo TWIIIINS</p>");
        return news.createNews(r);
    }
    @Test void saveQueuesAtomicallyAndEnglishSourceIsNeverRewritten() {
        ProjectDto p = project("Hello TWIIIINS"); ContentTranslation r = row(p);
        assertEquals("PENDING", r.getState()); verifyNoInteractions(gemini);
        complete(r, "en", "Model tried rewriting source", "Hallo TWIIIINS");
        r = row(p); assertEquals("COMPLETED", r.getState()); assertEquals("Hello TWIIIINS", r.getTextEn());
        assertEquals("Hallo TWIIIINS", r.getTextDe()); assertEquals("Hello TWIIIINS", projects.getProjectById(p.getId()).getTitle());
    }
    @Test void germanSourceAndNestedPathsAreSupported() {
        ProjectCreateRequest request = new ProjectCreateRequest(); request.setTitle("TWIIIINS"); request.setUrlSlug("nested");
        request.setDescriptions(List.of("Hallo Welt", "Zweiter Absatz"));
        ProjectCreateRequest.ReviewRequest review = new ProjectCreateRequest.ReviewRequest(); review.setText("Wunderbare Musik"); review.setSource("Named Reviewer");
        request.setReviews(List.of(review)); ProjectDto p = projects.createProject(request);
        ContentTranslation r = row("projects", p.getId(), "descriptions[0]"); complete(r, "de", "Hello world", "Changed source");
        assertEquals("Hallo Welt", rows.findById(r.getId()).orElseThrow().getTextDe());
        assertNotNull(row("projects", p.getId(), "reviews[0].text"));
        assertTrue(rows.findByKindAndContentId("projects", p.getId()).stream().noneMatch(v -> v.getFieldPath().contains("source")));
    }
    @Test void rollbackAlsoRollsBackQueuedWork() {
        new TransactionTemplate(transactions).executeWithoutResult(status -> { project("Rollback"); status.setRollbackOnly(); });
        assertEquals(0, projectRepository.count()); assertEquals(0, rows.count());
    }
    @Test void disabledSavesWorkAndEnablingPicksUpPersistedJobs() {
        settings.setApiKey(""); ProjectDto p = project("Hello"); assertEquals("DISABLED", row(p).getState());
        settings.setApiKey("configured"); complete(row(p), "en", "Hello", "Hallo"); assertEquals("COMPLETED", row(p).getState());
    }
    @Test void changedSourceInvalidatesAutomaticTranslationButPreservesManualCounterpart() {
        ProjectDto p = project("Original"); complete(row(p), "en", "Original", "Automatisch");
        ContentTranslation r = row(p); translations.edit(r.getId(), r.getVersion(), "Original", "Redaktionell");
        change(p, "Updated"); r = row(p); assertEquals("PENDING", r.getState()); assertTrue(r.isManualDe());
        complete(r, "en", "Updated", "Should not overwrite");
        assertEquals("Updated", row(p).getTextEn()); assertEquals("Redaktionell", row(p).getTextDe());
    }
    @Test void identicalSaveDoesNotRequeueOrRepeatModelCalls() {
        ProjectDto p = project("Unchanged"); complete(row(p), "en", "Unchanged", "Unverändert");
        change(p, "Unchanged"); worker.process(row(p).getId()); verify(gemini, times(1)).translate(any());
        assertEquals("COMPLETED", row(p).getState());
    }
    @Test void editDuringCallDiscardsStaleOutputWithoutRemovingNewJob() {
        ProjectDto p = project("Before");
        when(gemini.translate(any())).thenAnswer(invocation -> { change(p, "After"); return new GeminiTranslator.Result("en", "Before", "Vorher"); });
        worker.process(row(p).getId()); assertEquals("PENDING", row(p).getState()); assertEquals("After", row(p).getSourceText()); assertNull(row(p).getTextDe());
    }
    @Test void deletionDuringCallDiscardsOutput() {
        ProjectDto p = project("Delete me"); Long id = row(p).getId();
        when(gemini.translate(any())).thenAnswer(invocation -> { projects.deleteProject(p.getId()); return new GeminiTranslator.Result("en", "Delete me", "Löschen"); });
        worker.process(id); assertFalse(rows.existsById(id));
    }
    @Test void failedRequestsKeepOriginalAndRetryThreeTimesAtMost() {
        ProjectDto p = project("Keep source"); Long id = row(p).getId();
        when(gemini.translate(any())).thenThrow(new GeminiTranslator.Failure("GEMINI_HTTP_429", true));
        for (int attempt = 0; attempt < 4; attempt++) {
            ContentTranslation r = rows.findById(id).orElseThrow(); r.setNextAttempt(Instant.EPOCH); rows.save(r); worker.process(id);
        }
        ContentTranslation r = row(p); assertEquals("FAILED", r.getState()); assertEquals(4, r.getAttempts());
        assertEquals("Keep source", projects.getProjectById(p.getId()).getTitle());
        translations.retry(id); assertEquals("PENDING", row(p).getState()); assertEquals(0, row(p).getAttempts());
    }
    @Test void authenticationFailureDoesNotAutomaticallyRetry() {
        ProjectDto p = project("Keep source");
        when(gemini.translate(any())).thenThrow(new GeminiTranslator.Failure("GEMINI_HTTP_403", false));
        worker.process(row(p).getId()); worker.process(row(p).getId());
        assertEquals("FAILED", row(p).getState()); verify(gemini, times(1)).translate(any());
    }
    @Test void expiredLeaseIsRecoveredAndLiveLeaseIsNotDuplicated() {
        ProjectDto p = project("Recover"); ContentTranslation r = row(p); r.setState("PROCESSING"); r.setNextAttempt(Instant.now().plusSeconds(60)); rows.save(r);
        worker.process(r.getId()); verifyNoInteractions(gemini);
        r = row(p); r.setNextAttempt(Instant.EPOCH); rows.save(r); complete(r, "en", "Recover", "Wiederherstellen");
        assertEquals("COMPLETED", row(p).getState());
    }
    @Test void newsletterPairsAreWrittenIntoNativeFieldsAndVersionReturnedBySaveIsCurrent() {
        NewsDto n = draft("Hello", null); complete(row("news", n.getId(), "title"), "en", "Hello", "Hallo");
        complete(row("news", n.getId(), "bodyEn"), "en", "<p>Hello TWIIIINS</p>", "<p>Hallo TWIIIINS</p>");
        assertEquals("Hallo", news.adminGet(n.getId()).getTitleDe());
        boolean rendered = new TransactionTemplate(transactions).execute(status ->
            html.render(newsRepository.findById(n.getId()).orElseThrow(), "de", "https://example.com").contains("Hallo TWIIIINS"));
        assertTrue(rendered);
        NewsUpdateRequest update = new NewsUpdateRequest(); update.setTitle("Changed");
        NewsDto changed = news.updateNews(n.getId(), update);
        assertEquals("", changed.getTitleDe()); assertEquals(changed.getVersion(), news.adminGet(n.getId()).getVersion());
        complete(row("news", n.getId(), "title"), "en", "Changed", "Geändert");
        assertEquals("Geändert", news.adminGet(n.getId()).getTitleDe());
    }
    @Test void germanOnlyNewsAndGermanEnteredIntoEnglishTabAreDetected() {
        NewsDto n = draft(null, "Hallo"); complete(row("news", n.getId(), "title"), "de", "Hello", "Hallo");
        assertEquals("Hello", news.adminGet(n.getId()).getTitle()); assertEquals("Hallo", news.adminGet(n.getId()).getTitleDe());
        NewsDto other = draft("Guten Tag", null); complete(row("news", other.getId(), "title"), "de", "Good day", "Guten Tag");
        ContentTranslation r = row("news", other.getId(), "title"); assertTrue(r.isManualDe()); assertFalse(r.isManualEn());
        assertEquals("Good day", news.adminGet(other.getId()).getTitle()); assertEquals("Guten Tag", news.adminGet(other.getId()).getTitleDe());
        assertEquals(1, translations.list("news", other.getId(), 0).stream().filter(v -> v.field().equals("title")).count());
    }
    @Test void twoAuthoredLanguagesAreNeverSubmittedToGemini() {
        NewsDto n = draft("English", "Deutsch");
        for (ContentTranslation r : rows.findByKindAndContentId("news", n.getId())) worker.process(r.getId());
        NewsDto sameTitle = draft("TWIIIINS", "TWIIIINS");
        for (ContentTranslation r : rows.findByKindAndContentId("news", sameTitle.getId())) worker.process(r.getId());
        verifyNoInteractions(gemini); assertEquals("COMPLETED", row("news", n.getId(), "title").getState());
    }
    @Test void manualTranslationIsSanitizedAndStaleEditsConflict() {
        NewsDto n = draft("Title", null); ContentTranslation r = row("news", n.getId(), "bodyEn");
        translations.edit(r.getId(), r.getVersion(), r.getTextEn(), "<p>Hallo</p><script>alert(1)</script>");
        assertFalse(news.adminGet(n.getId()).getBodyDe().contains("script"));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> translations.edit(r.getId(), r.getVersion(), "<p>Stale</p>", "<p>Alt</p>"));
    }
    @Test void invalidTranslationDoesNotCorruptSourceProvenance() {
        NewsDto n = draft("Guten Tag", null); ContentTranslation r = row("news", n.getId(), "title");
        complete(r, "de", "x".repeat(256), "Guten Tag");
        r = row("news", n.getId(), "title"); assertEquals("PENDING", r.getState()); assertTrue(r.isManualEn());
        assertEquals("Guten Tag", news.adminGet(n.getId()).getTitle());
        assertTrue(translations.list("news", n.getId(), 0).stream().anyMatch(v -> v.field().equals("title")));
    }
    @Test void publicResponseContainsTranslationsAndAdminEndpointsRequireAuthentication() throws Exception {
        ProjectDto p = project("Hello"); complete(row(p), "en", "Hello", "Hallo");
        mvc.perform(get("/api/projects/" + p.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("Hello"))
            .andExpect(jsonPath("$.data.translations.title.de").value("Hallo"));
        mvc.perform(get("/api/admin/translations")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/translations/" + row(p).getId() + "/retry")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/admin/translations/" + row(p).getId()).contentType("application/json").content("{}"))
            .andExpect(status().isUnauthorized());
    }
    @Test void clearingSourceRemovesItsGeneratedCounterpartAndJob() {
        NewsDto n = draft("Title", null); complete(row("news", n.getId(), "bodyEn"), "en", "<p>Hello TWIIIINS</p>", "<p>Hallo TWIIIINS</p>");
        NewsUpdateRequest update = new NewsUpdateRequest(); update.setBodyEn(""); NewsDto saved = news.updateNews(n.getId(), update);
        assertEquals("", saved.getBodyEn()); assertEquals("", saved.getBodyDe());
        assertTrue(rows.findByKindAndContentId("news", n.getId()).stream().noneMatch(r -> r.getFieldPath().equals("bodyEn")));
    }
    @Test void generatedHtmlMustPreserveStructureAndLinkDestination() {
        NewsDto n = draft("Title", null); ContentTranslation r = row("news", n.getId(), "bodyEn");
        complete(r, "en", "<p>Hello TWIIIINS</p>", "<h2>Hallo TWIIIINS</h2>");
        assertEquals("PENDING", row("news", n.getId(), "bodyEn").getState());
        assertEquals("INVALID_TRANSLATION", row("news", n.getId(), "bodyEn").getErrorCode());
        assertEquals("", news.adminGet(n.getId()).getBodyDe());
    }
    @Test void simpleNewsRetainsGermanSourceWhenDescriptionChanges() {
        NewsCreateRequest request = new NewsCreateRequest(); request.setDate(LocalDate.now()); request.setTitle("Hallo"); request.setDescription("Deutscher Text");
        NewsDto n = news.createManualNews(request); complete(row("news", n.getId(), "bodyEn"), "de", "<p>German text</p>", "<p>Deutscher Text</p>");
        NewsUpdateRequest update = new NewsUpdateRequest(); update.setDescription("Neuer deutscher Text");
        news.updateManualNews(n.getId(), update);
        ContentTranslation r = row("news", n.getId(), "bodyEn"); assertFalse(r.isManualEn()); assertTrue(r.isManualDe());
        complete(r, "de", "<p>New German text</p>", "<p>Neuer deutscher Text</p>");
        assertEquals("<p>New German text</p>", news.adminGet(n.getId()).getBodyEn());
    }
}
