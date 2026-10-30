package com.twiiiins.translation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twiiiins.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import java.util.*;

/** Additive API metadata: authored fields remain unchanged for editors and existing clients. */
@ControllerAdvice @RequiredArgsConstructor
public class TranslationResponseAdvice implements ResponseBodyAdvice<Object> {
    private final TranslationRepository rows;
    private final TranslationFields fields;
    private final ObjectMapper json;
    public boolean supports(MethodParameter p, Class<? extends HttpMessageConverter<?>> converter) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converter);
    }
    public Object beforeBodyWrite(Object body, MethodParameter p, MediaType media, Class<? extends HttpMessageConverter<?>> c,
        ServerHttpRequest request, ServerHttpResponse response) {
        if (!(body instanceof ApiResponse<?> api) || api.getData() == null) return body;
        Map<String, Set<Long>> ids = new HashMap<>();
        collect(api.getData(), ids);
        if (ids.isEmpty()) return body;
        Map<String, List<ContentTranslation>> saved = new HashMap<>();
        ids.forEach((kind, contentIds) -> rows.findByKindAndContentIdIn(kind, contentIds).forEach(row ->
            saved.computeIfAbsent(kind + ":" + row.getContentId(), key -> new ArrayList<>()).add(row)));
        Object data = enrich(api.getData(), saved);
        if (data == api.getData()) return body;
        Map<String, Object> result = json.convertValue(api, new TypeReference<LinkedHashMap<String, Object>>() {});
        result.put("data", data); return result;
    }
    private void collect(Object data, Map<String, Set<Long>> ids) {
        if (data == null) return;
        if (data instanceof Collection<?> list) { list.forEach(item -> collect(item, ids)); return; }
        String kind = fields.dtoKind(data);
        if (kind != null && fields.id(data) != null) ids.computeIfAbsent(kind, key -> new HashSet<>()).add(fields.id(data));
        if (data instanceof com.twiiiins.dto.PhotoGroupDto group) collect(group.getPhotos(), ids);
    }
    private Object enrich(Object data, Map<String, List<ContentTranslation>> saved) {
        if (data == null) return null;
        if (data instanceof Collection<?> list) return list.stream().map(item -> enrich(item, saved)).toList();
        String kind = fields.dtoKind(data);
        if (kind == null) return data;
        Long id = fields.id(data);
        if (id == null) return data;
        Map<String, Object> result = json.convertValue(data, new TypeReference<LinkedHashMap<String, Object>>() {});
        Map<String, Object> translations = new LinkedHashMap<>();
        for (ContentTranslation row : saved.getOrDefault(kind + ":" + id, List.of())) {
            // Compare DTO snapshots without loading content. Pair hashes exclude generated counterparts.
            String source;
            if (TranslationFields.NEWS_PAIRS.containsKey(row.getFieldPath()) && kind.equals("news")) {
                String en = fields.read(data, row.getFieldPath()), de = fields.read(data, TranslationFields.NEWS_PAIRS.get(row.getFieldPath()));
                if (!row.isManualEn() && Objects.equals(en, row.getTextEn())) en = "";
                if (!row.isManualDe() && Objects.equals(de, row.getTextDe())) de = "";
                if (row.getFieldPath().equals("title") && TranslationFields.present(de) && en.equals(de) && !row.isManualEn()) en = "";
                try { source = json.writeValueAsString(new String[]{en, de}); } catch (Exception ex) { continue; }
            } else source = fields.read(data, row.getFieldPath());
            if (!Objects.equals(TranslationFields.hash(source), row.getSourceHash())) continue;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("state", row.getState());
            if ("COMPLETED".equals(row.getState())) { entry.put("en", row.getTextEn()); entry.put("de", row.getTextDe()); }
            translations.put(row.getFieldPath(), entry);
        }
        result.put("translations", translations);
        if (data instanceof com.twiiiins.dto.PhotoGroupDto group) result.put("photos", enrich(group.getPhotos(), saved));
        return result;
    }
}
