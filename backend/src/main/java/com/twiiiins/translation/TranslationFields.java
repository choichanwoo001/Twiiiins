package com.twiiiins.translation;

import com.twiiiins.entity.*;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Explicit allowlist: media links, identifiers and private/user data never enter a prompt. */
@Component @RequiredArgsConstructor
public class TranslationFields {
    private final EntityManager entities;
    static final Map<String, Class<?>> TYPES = Map.ofEntries(
        Map.entry("projects", Project.class), Map.entry("concerts", Concert.class), Map.entry("news", News.class),
        Map.entry("music", Music.class), Map.entry("videos", Video.class), Map.entry("photoGroups", PhotoGroup.class),
        Map.entry("photos", Photo.class), Map.entry("contacts", Contact.class), Map.entry("contactInfo", ContactInfo.class),
        Map.entry("downloads", DownloadFile.class), Map.entry("equipment", Equipment.class));
    private static final Map<String, List<String>> FIELDS = Map.ofEntries(
        Map.entry("projects", List.of("title", "subtitle", "location", "thankYouText")),
        Map.entry("concerts", List.of("name", "location", "ticketInfo", "fullLocation", "collaborationInfo")),
        Map.entry("music", List.of("title")), Map.entry("videos", List.of("title")),
        Map.entry("photoGroups", List.of("title")), Map.entry("photos", List.of("altText")),
        Map.entry("contacts", List.of("role")), Map.entry("contactInfo", List.of("role", "introText")),
        Map.entry("downloads", List.of("name")), Map.entry("equipment", List.of("name")));
    static final Map<String, String> NEWS_PAIRS = Map.of("title", "titleDe", "bodyEn", "bodyDe",
        "eventWhenEn", "eventWhenDe", "eventLocationEn", "eventLocationDe", "ctaLabelEn", "ctaLabelDe");
    public String kind(Object entity) {
        return TYPES.entrySet().stream().filter(e -> e.getValue().isInstance(entity)).map(Map.Entry::getKey).findFirst().orElse(null);
    }
    public String dtoKind(Object value) {
        String name = value.getClass().getSimpleName();
        return TYPES.entrySet().stream().filter(e -> name.equals(e.getValue().getSimpleName() + "Dto") || e.getValue().isInstance(value))
            .map(Map.Entry::getKey).findFirst().orElse(null);
    }
    public Object find(String kind, Long id) {
        Class<?> type = TYPES.get(kind);
        if (type == null) throw new IllegalArgumentException("Unknown content kind");
        return entities.find(type, id);
    }
    public Object lockEntity(String kind, Long id) {
        return entities.find(TYPES.get(kind), id, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    }
    public Long id(Object entity) { return (Long) new BeanWrapperImpl(entity).getPropertyValue("id"); }
    public String read(Object entity, String path) {
        try { Object value = new BeanWrapperImpl(entity).getPropertyValue(path); return value instanceof String s ? s : ""; }
        catch (org.springframework.beans.BeansException | IndexOutOfBoundsException ex) { return ""; }
    }
    public void write(Object entity, String path, String value) { new BeanWrapperImpl(entity).setPropertyValue(path, value); }
    public Map<String, String> texts(Object entity) {
        String kind = kind(entity);
        Map<String, String> result = new LinkedHashMap<>();
        if (entity instanceof News) {
            NEWS_PAIRS.keySet().stream().sorted().forEach(path -> result.put(path, read(entity, path)));
            result.put("description", read(entity, "description"));
        } else {
            FIELDS.getOrDefault(kind, List.of()).forEach(path -> result.put(path, read(entity, path)));
            if (entity instanceof Project p) {
                for (int i = 0; i < p.getDescriptions().size(); i++) result.put("descriptions[" + i + "]", p.getDescriptions().get(i));
                for (int i = 0; i < p.getReviews().size(); i++) result.put("reviews[" + i + "].text", p.getReviews().get(i).getText());
            }
        }
        return result;
    }
    public int maxLength(String kind, String path) {
        if (path.startsWith("body")) return 100000;
        if (path.startsWith("descriptions[") || path.equals("thankYouText")) return 2000;
        if (path.equals("description") || path.startsWith("reviews[")) return 1000;
        if (path.equals("introText") || path.equals("collaborationInfo") || path.equals("ticketInfo")) return 2000;
        return 255;
    }
    public static String hash(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    static boolean present(String text) { return text != null && !text.isBlank() && !org.jsoup.Jsoup.parse(text).text().isBlank(); }
}
