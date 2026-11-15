package com.twiiiins.translation;

import com.twiiiins.entity.News;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service @RequiredArgsConstructor
public class TranslationService {
    private final java.time.Clock clock;
    private final TranslationRepository rows;
    private final TranslationFields fields;
    private final TranslationSettings settings;
    private final TranslationContent content;

    /** Called by content services inside the original save transaction. No network I/O here. */
    @Transactional
    public <T> T capture(T entity) {
        String kind = fields.kind(entity);
        Long id = fields.id(entity);
        // Serialize content edits with worker completion, then lock translation rows.
        fields.lockEntity(kind, id);
        Map<String, ContentTranslation> existing = new HashMap<>();
        rows.lockByKindAndContentId(kind, id).forEach(row -> existing.put(row.getFieldPath(), row));
        Map<String, String> texts = fields.texts(entity);
        for (var entry : texts.entrySet()) {
            String path = entry.getKey();
            ContentTranslation saved = existing.remove(path);
            ContentTranslation row = saved == null ? new ContentTranslation() : saved;
            row.setFieldPath(path);
            switch (content.capture(entity, row, entry.getValue())) {
                case REMOVE -> {
                    if (saved != null) rows.delete(saved);
                }
                case SAVE -> {
                    row.setKind(kind);
                    row.setContentId(id);
                    reset(row);
                    rows.save(row);
                }
                case UNCHANGED -> { }
            }
        }
        existing.values().forEach(rows::delete);
        return entity;
    }
    private void reset(ContentTranslation row) {
        row.setState(row.isManualEn() && row.isManualDe() ? "COMPLETED" : settings.available() ? "PENDING" : "DISABLED");
        row.setAttempts(0); row.setNextAttempt(clock.instant()); row.setErrorCode(null);
    }
    @Transactional public void remove(String kind, Long id) {
        fields.lockEntity(kind, id);
        // A worker may have claimed a row since a prior read in this transaction.
        // Delete the current rows atomically rather than checking stale entity versions.
        rows.deleteByKindAndContentId(kind, id);
    }

    public record View(Long id, String kind, Long contentId, String field, String source, String sourceHash,
                       String en, String de, boolean manualEn, boolean manualDe, String state, String errorCode, Long version) {}
    View view(ContentTranslation r) {
        return new View(r.getId(), r.getKind(), r.getContentId(), r.getFieldPath(), r.getSourceText(), r.getSourceHash(),
            r.getTextEn(), r.getTextDe(), r.isManualEn(), r.isManualDe(), r.getState(), r.getErrorCode(), r.getVersion());
    }
    @Transactional(readOnly = true)
    public List<View> list(String kind, Long contentId, int page) {
        if (kind != null && !TranslationFields.TYPES.containsKey(kind)) throw new IllegalArgumentException("Unknown content kind");
        List<ContentTranslation> found = rows.search(kind, contentId,
            org.springframework.data.domain.PageRequest.of(page, 100, org.springframework.data.domain.Sort.by("id").descending())).getContent();
        return found.stream().filter(r -> kind == null || kind.equals(r.getKind()))
            .filter(r -> content.isCurrent(fields.find(r.getKind(), r.getContentId()), r)).map(this::view).toList();
    }
    @Transactional public View retry(Long id) {
        ContentTranslation before = rows.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Object entity = fields.lockEntity(before.getKind(), before.getContentId());
        ContentTranslation r = rows.lock(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!content.isCurrent(entity, r)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Content changed. Reload before retrying.");
        if (!Set.of("FAILED", "DISABLED").contains(r.getState())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Translation is already queued or complete.");
        reset(r); return view(r);
    }
    @Transactional public View edit(Long id, Long version, String en, String de) {
        ContentTranslation before = rows.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Object entity = fields.lockEntity(before.getKind(), before.getContentId());
        ContentTranslation r = rows.lock(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Objects.equals(version, r.getVersion()) || !content.isCurrent(entity, r))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Content changed. Reload before saving.");
        content.applyManual(entity, r, en, de);
        r.setNextAttempt(clock.instant());
        rows.flush(); return view(r);
    }
    /** The simple News editor stores its source body through description, even after language detection. */
    @Transactional public void manualNewsBody(News news, String body) {
        var row = rows.findByKindAndContentId("news", news.getId()).stream().filter(r -> r.getFieldPath().equals("bodyEn")).findFirst().orElse(null);
        content.updateNewsDescription(news, row, body);
    }
}
