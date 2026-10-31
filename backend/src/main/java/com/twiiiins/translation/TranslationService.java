package com.twiiiins.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twiiiins.entity.News;
import com.twiiiins.newsletter.NewsletterContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.*;
import static com.twiiiins.translation.TranslationFields.present;

@Service @RequiredArgsConstructor
public class TranslationService {
    private final java.time.Clock clock;
    private final TranslationRepository rows;
    private final TranslationFields fields;
    private final TranslationSettings settings;
    private final NewsletterContent html;
    private final ObjectMapper json;

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
            ContentTranslation row = existing.remove(path);
            String source = snapshot(entity, path, row);
            boolean paired = paired(entity, path);
            if (paired) {
                String[] authored = pairSource(entity, path, row);
                if (!present(authored[0]) && !present(authored[1])) {
                    if (row != null) rows.delete(row);
                    fields.write(entity, path, "");
                    fields.write(entity, TranslationFields.NEWS_PAIRS.get(path), "");
                    continue;
                }
            }
            if (!present(entry.getValue()) && (!paired || !present(fields.read(entity, TranslationFields.NEWS_PAIRS.get(path))))) {
                if (row != null) rows.delete(row);
                continue;
            }
            if (row != null && Objects.equals(row.getSourceHash(), TranslationFields.hash(source))) continue;
            if (row == null) {
                row = new ContentTranslation(); row.setKind(kind); row.setContentId(id); row.setFieldPath(path);
            }
            if (paired) {
                // An unchanged generated counterpart must never become an authored input on re-save.
                String[] authored = pairSource(entity, path, row);
                row.setTextEn(authored[0]); row.setTextDe(authored[1]);
                row.setManualEn(present(authored[0])); row.setManualDe(present(authored[1]));
                row.setSourceLanguage(present(authored[0]) ? "en" : "de");
                if (!row.isManualEn()) fields.write(entity, path, path.equals("title") ? authored[1] : "");
                if (!row.isManualDe()) fields.write(entity, TranslationFields.NEWS_PAIRS.get(path), "");
            } else {
                if (!row.isManualEn()) row.setTextEn(null);
                if (!row.isManualDe()) row.setTextDe(null);
            }
            row.setSourceText(source); row.setSourceHash(TranslationFields.hash(source));
            reset(row);
            rows.save(row);
        }
        existing.values().forEach(rows::delete);
        return entity;
    }
    private void reset(ContentTranslation row) {
        row.setState(row.isManualEn() && row.isManualDe() ? "COMPLETED" : settings.available() ? "PENDING" : "DISABLED");
        row.setAttempts(0); row.setNextAttempt(clock.instant()); row.setErrorCode(null);
    }
    boolean paired(Object entity, String path) { return entity instanceof News && TranslationFields.NEWS_PAIRS.containsKey(path); }
    private String[] pairSource(Object entity, String path, ContentTranslation row) {
        String en = fields.read(entity, path), de = fields.read(entity, TranslationFields.NEWS_PAIRS.get(path));
        if (row != null) {
            if (!row.isManualEn() && Objects.equals(en, row.getTextEn())) en = "";
            if (!row.isManualDe() && Objects.equals(de, row.getTextDe())) de = "";
        }
        // News.title is non-nullable and older German-only drafts copy titleDe into title.
        if (path.equals("title") && present(de) && en.equals(de)
            && ((entity instanceof News n && n.isTranslationTitleFallback()) || (row != null && row.getId() != null && !row.isManualEn()))) en = "";
        return new String[]{en, de};
    }
    String snapshot(Object entity, String path, ContentTranslation row) {
        if (!paired(entity, path)) return fields.read(entity, path);
        try { return json.writeValueAsString(pairSource(entity, path, row)); }
        catch (JsonProcessingException e) { throw new IllegalStateException(e); }
    }
    boolean current(Object entity, ContentTranslation row) {
        return entity != null && fields.texts(entity).containsKey(row.getFieldPath())
            && Objects.equals(row.getSourceHash(), TranslationFields.hash(snapshot(entity, row.getFieldPath(), row)));
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
            .filter(r -> current(fields.find(r.getKind(), r.getContentId()), r)).map(this::view).toList();
    }
    @Transactional public View retry(Long id) {
        ContentTranslation before = rows.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Object entity = fields.lockEntity(before.getKind(), before.getContentId());
        ContentTranslation r = rows.lock(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!current(entity, r)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Content changed. Reload before retrying.");
        if (!Set.of("FAILED", "DISABLED").contains(r.getState())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Translation is already queued or complete.");
        reset(r); return view(r);
    }
    @Transactional public View edit(Long id, Long version, String en, String de) {
        ContentTranslation before = rows.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Object entity = fields.lockEntity(before.getKind(), before.getContentId());
        ContentTranslation r = rows.lock(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Objects.equals(version, r.getVersion()) || !current(entity, r))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Content changed. Reload before saving.");
        String cleanEn = validate(r, en), cleanDe = validate(r, de);
        if (!present(cleanEn) || !present(cleanDe)) throw new IllegalArgumentException("Both languages are required.");
        // Only changed text becomes manual; approving one side must not freeze the automatic other side.
        if (!Objects.equals(cleanEn, r.getTextEn())) r.setManualEn(true);
        if (!Objects.equals(cleanDe, r.getTextDe())) r.setManualDe(true);
        r.setTextEn(cleanEn); r.setTextDe(cleanDe);
        if (paired(entity, r.getFieldPath())) {
            fields.write(entity, r.getFieldPath(), cleanEn);
            fields.write(entity, TranslationFields.NEWS_PAIRS.get(r.getFieldPath()), cleanDe);
            r.setSourceText(snapshot(entity, r.getFieldPath(), r));
            r.setSourceHash(TranslationFields.hash(r.getSourceText()));
        }
        r.setState("COMPLETED"); r.setErrorCode(null); r.setNextAttempt(clock.instant());
        rows.flush(); return view(r);
    }
    String validate(ContentTranslation row, String value) {
        if (value == null || value.length() > fields.maxLength(row.getKind(), row.getFieldPath()))
            throw new IllegalArgumentException("Invalid translated text length");
        return row.getFieldPath().startsWith("body") ? html.clean(value) : value;
    }
    void validateStructure(ContentTranslation row, String original, String translated) {
        if (row.getFieldPath().startsWith("body")) {
            if (!structure(html.clean(original)).equals(structure(translated)))
                throw new IllegalArgumentException("HTML structure or link was changed");
        }
        var urls = java.util.regex.Pattern.compile("https?://[^\\s<>\"']+").matcher(original);
        while (urls.find()) if (!translated.contains(urls.group())) throw new IllegalArgumentException("URL was changed");
    }
    private List<String> structure(String value) {
        return org.jsoup.Jsoup.parse(value).body().getAllElements().stream()
            .map(e -> e.tagName() + (e.hasAttr("href") ? ":" + e.attr("href") : "")).toList();
    }
    /** The simple News editor stores its source body through description, even after language detection. */
    @Transactional public void manualNewsBody(News news, String body) {
        var row = rows.findByKindAndContentId("news", news.getId()).stream().filter(r -> r.getFieldPath().equals("bodyEn")).findFirst().orElse(null);
        if (row != null && row.isManualDe() && !row.isManualEn()) news.setBodyDe(body);
        else news.setBodyEn(body);
    }
}
