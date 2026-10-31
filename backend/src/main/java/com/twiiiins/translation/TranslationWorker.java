package com.twiiiins.translation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.*;

@Component @RequiredArgsConstructor
public class TranslationWorker {
    private final java.time.Clock clock;
    private final TranslationRepository rows;
    private final TranslationFields fields;
    private final TranslationService service;
    private final TranslationSettings settings;
    private final GeminiTranslator gemini;
    private final PlatformTransactionManager transactions;
    @Bean public ThreadPoolTaskScheduler taskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler(); scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("scheduled-"); return scheduler;
    }
    @Bean public ThreadPoolTaskScheduler translationScheduler() {
        var scheduler = new ThreadPoolTaskScheduler(); scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("translation-"); return scheduler;
    }
    @Scheduled(fixedDelayString = "${app.translation.worker-delay:3000}", initialDelayString = "${app.translation.worker-initial-delay:15000}", scheduler = "translationScheduler")
    public void tick() {
        if (!settings.available()) return;
        for (Long id : rows.due(clock.instant(), org.springframework.data.domain.PageRequest.of(0, 1))) process(id);
    }
    public void process(Long id) {
        if (!settings.available()) return;
        var tx = new TransactionTemplate(transactions);
        ContentTranslation job = tx.execute(status -> {
            ContentTranslation r = rows.lock(id).orElse(null);
            if (r == null || !Set.of("PENDING", "PROCESSING", "DISABLED").contains(r.getState()) || r.getNextAttempt().isAfter(clock.instant())) return null;
            r.setState("PROCESSING"); r.setAttempts(r.getAttempts() + 1);
            r.setNextAttempt(clock.instant().plusSeconds(180)); rows.flush();
            return r;
        });
        if (job == null) return;
        GeminiTranslator.Result result = null;
        GeminiTranslator.Failure failure = null;
        try { result = gemini.translate(job); }
        catch (GeminiTranslator.Failure ex) { failure = ex; }
        catch (Exception ex) { failure = new GeminiTranslator.Failure("TRANSLATION_ERROR", true); }
        final var output = result; final var error = failure;
        tx.executeWithoutResult(status -> {
            // Content lock first: the same order used by saves and manual edits.
            Object entity = fields.lockEntity(job.getKind(), job.getContentId());
            ContentTranslation row = rows.lock(id).orElse(null);
            if (row == null || !Objects.equals(row.getVersion(), job.getVersion()) || !"PROCESSING".equals(row.getState())) return;
            if (!service.current(entity, row)) { rows.delete(row); return; }
            if (error != null) { fail(row, error); return; }
            try {
                boolean manualEn = row.isManualEn(), manualDe = row.isManualDe();
                String authoredEn = row.getTextEn(), authoredDe = row.getTextDe();
                if (service.paired(entity, row.getFieldPath()) && manualEn != manualDe) {
                    String original = row.isManualEn() ? row.getTextEn() : row.getTextDe();
                    // A German article entered into the default English tab is still German source text.
                    manualEn = "en".equals(output.sourceLanguage());
                    manualDe = "de".equals(output.sourceLanguage());
                    if (manualEn) authoredEn = original; else authoredDe = original;
                }
                String en = manualEn ? authoredEn : service.validate(row, output.en());
                String de = manualDe ? authoredDe : service.validate(row, output.de());
                if (!service.paired(entity, row.getFieldPath())) {
                    // Never trust a model to rewrite the source language.
                    if ("en".equals(output.sourceLanguage()) && !row.isManualEn()) en = row.getSourceText();
                    if ("de".equals(output.sourceLanguage()) && !row.isManualDe()) de = row.getSourceText();
                }
                if (!TranslationFields.present(en) || !TranslationFields.present(de)) throw new IllegalArgumentException("Empty translation");
                String original = service.paired(entity, row.getFieldPath())
                    ? (row.isManualEn() ? row.getTextEn() : row.getTextDe()) : row.getSourceText();
                if (!manualEn) service.validateStructure(row, original, en);
                if (!manualDe) service.validateStructure(row, original, de);
                row.setManualEn(manualEn); row.setManualDe(manualDe);
                row.setTextEn(en); row.setTextDe(de); row.setSourceLanguage(output.sourceLanguage());
                if (service.paired(entity, row.getFieldPath())) {
                    fields.write(entity, row.getFieldPath(), en);
                    fields.write(entity, TranslationFields.NEWS_PAIRS.get(row.getFieldPath()), de);
                    row.setSourceText(service.snapshot(entity, row.getFieldPath(), row));
                    row.setSourceHash(TranslationFields.hash(row.getSourceText()));
                }
                row.setState("COMPLETED"); row.setErrorCode(null);
            } catch (IllegalArgumentException ex) { fail(row, new GeminiTranslator.Failure("INVALID_TRANSLATION", true)); }
        });
    }
    private void fail(ContentTranslation row, GeminiTranslator.Failure error) {
        row.setErrorCode(error.code);
        boolean retry = error.retryable && row.getAttempts() <= 3;
        row.setState(retry ? "PENDING" : "FAILED");
        long[] delays = {30, 120, 600};
        row.setNextAttempt(clock.instant().plusSeconds(delays[Math.min(Math.max(row.getAttempts() - 1, 0), 2)]));
    }
}
