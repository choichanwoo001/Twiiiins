package com.twiiiins.translation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;

@Component @RequiredArgsConstructor
public class TranslationWorker {
    private final java.time.Clock clock;
    private final TranslationRepository rows;
    private final TranslationFields fields;
    private final TranslationContent content;
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
            if (!content.isCurrent(entity, row)) { rows.delete(row); return; }
            if (error != null) { fail(row, error); return; }
            try {
                content.applyGenerated(entity, row, output);
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
