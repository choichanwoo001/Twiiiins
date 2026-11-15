package com.twiiiins.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twiiiins.dto.NewsDto;
import com.twiiiins.entity.News;
import com.twiiiins.newsletter.NewsletterContent;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.twiiiins.translation.TranslationFields.present;

@Component
@RequiredArgsConstructor
class TranslationContent {
    enum Capture { REMOVE, UNCHANGED, SAVE }

    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"']+");

    private final TranslationFields fields;
    private final NewsletterContent html;
    private final ObjectMapper json;

    Capture capture(Object entity, ContentTranslation row, String value) {
        String path = row.getFieldPath();
        String pairedPath = pairedPath(entity, path);
        if (pairedPath == null) {
            if (!present(value)) return Capture.REMOVE;
            if (matches(row, value)) return Capture.UNCHANGED;
            if (!row.isManualEn()) row.setTextEn(null);
            if (!row.isManualDe()) row.setTextDe(null);
            setSource(row, value);
            return Capture.SAVE;
        }

        String[] authored = pairSource(entity, path, pairedPath, row);
        boolean manualEn = present(authored[0]);
        boolean manualDe = present(authored[1]);
        if (!manualEn && !manualDe) {
            fields.write(entity, path, "");
            fields.write(entity, pairedPath, "");
            return Capture.REMOVE;
        }

        String source = encodePair(authored);
        if (matches(row, source)) return Capture.UNCHANGED;
        row.setTextEn(authored[0]);
        row.setTextDe(authored[1]);
        row.setManualEn(manualEn);
        row.setManualDe(manualDe);
        row.setSourceLanguage(manualEn ? "en" : "de");
        if (!manualEn) fields.write(entity, path, "title".equals(path) ? authored[1] : "");
        if (!manualDe) fields.write(entity, pairedPath, "");
        setSource(row, source);
        return Capture.SAVE;
    }

    boolean isCurrent(Object entity, ContentTranslation row) {
        return entity != null && fields.texts(entity).containsKey(row.getFieldPath())
            && matches(row, snapshot(entity, row));
    }

    boolean matchesResponse(Object dto, ContentTranslation row) {
        try {
            return matches(row, snapshot(dto, row));
        } catch (IllegalStateException ex) {
            // Preserve the response if a snapshot cannot be serialized.
            return false;
        }
    }

    void applyManual(Object entity, ContentTranslation row, String en, String de) {
        String cleanEn = validate(row, en);
        String cleanDe = validate(row, de);
        if (!present(cleanEn) || !present(cleanDe)) throw new IllegalArgumentException("Both languages are required.");
        // Only changed text becomes manual so the other language can still be regenerated.
        if (!Objects.equals(cleanEn, row.getTextEn())) row.setManualEn(true);
        if (!Objects.equals(cleanDe, row.getTextDe())) row.setManualDe(true);
        complete(entity, row, cleanEn, cleanDe);
    }

    void applyGenerated(Object entity, ContentTranslation row, GeminiTranslator.Result output) {
        boolean paired = pairedPath(entity, row.getFieldPath()) != null;
        boolean manualEn = row.isManualEn();
        boolean manualDe = row.isManualDe();
        String authoredEn = row.getTextEn();
        String authoredDe = row.getTextDe();
        String original = paired ? (manualEn ? authoredEn : authoredDe) : row.getSourceText();

        if (paired && manualEn != manualDe) {
            // A German article entered into the default English tab is still German source text.
            manualEn = "en".equals(output.sourceLanguage());
            manualDe = "de".equals(output.sourceLanguage());
            if (manualEn) authoredEn = original;
            else authoredDe = original;
        }
        String en = manualEn ? authoredEn : validate(row, output.en());
        String de = manualDe ? authoredDe : validate(row, output.de());
        if (!paired) {
            if ("en".equals(output.sourceLanguage()) && !manualEn) en = original;
            if ("de".equals(output.sourceLanguage()) && !manualDe) de = original;
        }
        if (!present(en) || !present(de)) throw new IllegalArgumentException("Empty translation");
        if (!manualEn) validateStructure(row, original, en);
        if (!manualDe) validateStructure(row, original, de);

        row.setManualEn(manualEn);
        row.setManualDe(manualDe);
        row.setSourceLanguage(output.sourceLanguage());
        complete(entity, row, en, de);
    }

    void updateNewsDescription(News news, ContentTranslation body, String value) {
        if (body != null && body.isManualDe() && !body.isManualEn()) news.setBodyDe(value);
        else news.setBodyEn(value);
    }

    private void complete(Object entity, ContentTranslation row, String en, String de) {
        row.setTextEn(en);
        row.setTextDe(de);
        String path = row.getFieldPath();
        String pairedPath = pairedPath(entity, path);
        if (pairedPath != null) {
            fields.write(entity, path, en);
            fields.write(entity, pairedPath, de);
            setSource(row, snapshot(entity, row));
        }
        row.setState("COMPLETED");
        row.setErrorCode(null);
    }

    private String snapshot(Object entity, ContentTranslation row) {
        String path = row.getFieldPath();
        String pairedPath = pairedPath(entity, path);
        return pairedPath == null ? fields.read(entity, path) : encodePair(pairSource(entity, path, pairedPath, row));
    }

    private static String pairedPath(Object entity, String path) {
        return entity instanceof News || entity instanceof NewsDto ? TranslationFields.NEWS_PAIRS.get(path) : null;
    }

    private String[] pairSource(Object entity, String path, String pairedPath, ContentTranslation row) {
        String en = fields.read(entity, path);
        String de = fields.read(entity, pairedPath);
        if ("title".equals(path) && entity instanceof NewsDto dto && dto.getTitleBeforeLocalization() != null) {
            en = dto.getTitleBeforeLocalization();
        }
        // Exclude unchanged generated counterparts from the authored source.
        if (!row.isManualEn() && Objects.equals(en, row.getTextEn())) en = "";
        if (!row.isManualDe() && Objects.equals(de, row.getTextDe())) de = "";
        // News.title is non-nullable and older German-only drafts copy titleDe into title.
        boolean titleFallback = entity instanceof News news && news.isTranslationTitleFallback();
        if ("title".equals(path) && present(de) && en.equals(de)
            && (titleFallback || (row.getId() != null && !row.isManualEn()))) en = "";
        return new String[]{en, de};
    }

    private String encodePair(String[] pair) {
        try {
            return json.writeValueAsString(pair);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static boolean matches(ContentTranslation row, String source) {
        return Objects.equals(row.getSourceHash(), TranslationFields.hash(source));
    }

    private static void setSource(ContentTranslation row, String source) {
        row.setSourceText(source);
        row.setSourceHash(TranslationFields.hash(source));
    }

    private String validate(ContentTranslation row, String value) {
        if (value == null || value.length() > fields.maxLength(row.getKind(), row.getFieldPath())) {
            throw new IllegalArgumentException("Invalid translated text length");
        }
        return row.getFieldPath().startsWith("body") ? html.clean(value) : value;
    }

    private void validateStructure(ContentTranslation row, String original, String translated) {
        if (row.getFieldPath().startsWith("body") && !structure(html.clean(original)).equals(structure(translated))) {
            throw new IllegalArgumentException("HTML structure or link was changed");
        }
        var urls = URL.matcher(original);
        while (urls.find()) {
            if (!translated.contains(urls.group())) throw new IllegalArgumentException("URL was changed");
        }
    }

    private static List<String> structure(String value) {
        return Jsoup.parse(value).body().getAllElements().stream()
            .map(element -> element.tagName() + (element.hasAttr("href") ? ":" + element.attr("href") : ""))
            .toList();
    }
}
