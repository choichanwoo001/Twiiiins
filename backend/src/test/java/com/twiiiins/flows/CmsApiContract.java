package com.twiiiins.flows;

import com.fasterxml.jackson.databind.*;
import com.twiiiins.entity.User;
import com.twiiiins.repository.UserRepository;
import com.twiiiins.security.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DynamicTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/** Same HTTP/security/persistence contracts run on H2 and real MySQL. */
public abstract class CmsApiContract {
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired AdminSessionRepository sessions;
    @Autowired com.twiiiins.service.FileUploadService uploads;
    @Autowired @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings;

    record Resource(String path, String key, Map<String,Object> input) {}
    static Stream<Resource> resources() {
        return Stream.of(
            new Resource("/api/concerts", "name", Map.of("name","Flow Concert", "location","Seoul", "date","2026-10-31", "isPast",false)),
            new Resource("/api/projects", "title", Map.of("title","Flow Project", "urlSlug","flow-project", "premiereDate","2026-10-31", "descriptions",List.of("Hello", "Hallo"), "reviews",List.of(Map.of("text","Excellent", "source","Press")))),
            new Resource("/api/media/music", "title", Map.of("title","Flow Music", "artist","TWIIIINS", "coverUrl","/uploads/cover.png")),
            new Resource("/api/media/videos", "title", Map.of("title","Flow Video", "embedUrl","https://www.youtube.com/embed/abcdefghijk")),
            new Resource("/api/media/equipment", "name", Map.of("name","Flow Equipment")),
            new Resource("/api/media/download-files", "name", Map.of("name","Flow Rider", "fileUrl","/uploads/rider.pdf", "downloadSource","upload")),
            new Resource("/api/media/contacts", "name", Map.of("name","Flow Contact", "email","booking@example.com", "role","Booking")),
            new Resource("/api/media/news", "title", Map.of("title","Flow News", "date","2026-10-31", "description","Hello")),
            new Resource("/api/media/photos/groups", "title", Map.of("title","Flow Photos"))
        );
    }
    protected JsonNode data(MockHttpServletRequestBuilder request) throws Exception {
        return json.readTree(mvc.perform(request.with(user("contract-admin").roles("ADMIN")))
            .andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString()).path("data");
    }
    MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder req, Object value) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(value));
    }
    @TestFactory Stream<DynamicTest> everyContentSupportsPersistentCrudAndRejectsBadInput() {
        return resources().map(r -> DynamicTest.dynamicTest(r.path(), () -> {
            mvc.perform(body(post(r.path()), Map.of()).with(user("admin").roles("ADMIN"))).andExpect(status().isBadRequest());
            JsonNode created = data(body(post(r.path()), r.input())); long id = created.path("id").asLong(); assertTrue(id > 0);
            String path = r.path()+"/"+id;
            try {
                mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.data."+r.key()).value(r.input().get(r.key())));
                Map<String,Object> update = new HashMap<>(r.input()); update.put(r.key(), "Updated 한국어 Grüße");
                if (r.path().endsWith("/news")) update.put("version", created.path("version").asLong());
                data(body(put(path), update));
                // A fresh request must observe committed state, not only a success response.
                mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.data."+r.key()).value("Updated 한국어 Grüße"));
                var invalid = new HashMap<>(update); invalid.put(r.key(), "x".repeat(256));
                mvc.perform(body(put(path),invalid).with(user("admin").roles("ADMIN"))).andExpect(status().isBadRequest());
                mvc.perform(get(path)).andExpect(jsonPath("$.data."+r.key()).value("Updated 한국어 Grüße"));
            } finally { data(delete(path)); }
            mvc.perform(get(path)).andExpect(status().isNotFound());
        }));
    }
    @TestFactory Stream<DynamicTest> everyMappedProtectedWriteRejectsAnonymousAndForgedTokens() {
        return mappings.getHandlerMethods().entrySet().stream()
            .filter(e -> e.getValue().getBeanType().getPackageName().startsWith("com.twiiiins"))
            .flatMap(e -> e.getKey().getPatternValues().stream().flatMap(path -> e.getKey().getMethodsCondition().getMethods().stream()
                .filter(method -> Set.of(RequestMethod.POST,RequestMethod.PUT,RequestMethod.PATCH,RequestMethod.DELETE).contains(method))
                .filter(method -> !path.equals("/api/auth/login") && !path.startsWith("/api/newsletter/"))
                .map(method -> DynamicTest.dynamicTest(method+" "+path, () -> {
                    String concrete = path.replaceAll("\\{[^}]+}", "999999");
                    for (String token : List.of("", "Bearer forged-token")) {
                        mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method.name()), concrete)
                            .header("Authorization",token).contentType(MediaType.APPLICATION_JSON).content("{}"))
                            .andExpect(status().isUnauthorized());
                    }
                }))));
    }
    @Test void actualLoginExpiryAndRevocation() throws Exception {
        User account = users.findByUsername("contract-login").orElseGet(User::new);
        account.setUsername("contract-login"); account.setPassword(passwords.encode("local-contract-password")); users.save(account);
        mvc.perform(body(post("/api/auth/login"), Map.of("username","contract-login","password","wrong"))).andExpect(status().isBadRequest());
        JsonNode login = json.readTree(mvc.perform(body(post("/api/auth/login"),Map.of("username","contract-login","password","local-contract-password")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("data");
        String token = login.path("token").asText(); assertFalse(token.isBlank());
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").header("Authorization","Bearer "+token)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        AdminSession expired = new AdminSession(); expired.setTokenHash(Tokens.hash("expired-token")); expired.setUsername(account.getUsername());
        expired.setExpiresAt(java.time.Instant.EPOCH); sessions.save(expired);
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer expired-token")).andExpect(status().isUnauthorized());
        sessions.deleteById(expired.getTokenHash());
    }
    @Test void projectSlugAndImageReplacementRemainConsistent() throws Exception {
        JsonNode project = data(body(post("/api/projects"),Map.of("title","Project", "urlSlug","image-contract")));
        String path = "/api/projects/"+project.path("id").asLong();
        try {
            data(body(put(path+"/images"),Map.of("imageUrls",List.of("/uploads/a.png","/uploads/b.png"))));
            mvc.perform(get("/api/projects/slug/image-contract")).andExpect(status().isOk()).andExpect(jsonPath("$.data.imageUrls.length()").value(2));
            data(body(put(path+"/images"),Map.of("imageUrls",List.of())));
            mvc.perform(get(path)).andExpect(jsonPath("$.data.imageUrls.length()").value(0));
        } finally { data(delete(path)); }
        mvc.perform(get("/api/projects/slug/image-contract")).andExpect(status().isNotFound());
    }
    @Test void concertDateBoundaryAndManualMoves() throws Exception {
        List<Long> ids = new ArrayList<>();
        try {
            for (String date : List.of("2026-10-30","2026-10-31","2026-11-01")) ids.add(data(body(post("/api/concerts"),Map.of("name","Boundary", "location","Berlin", "date",date))).path("id").asLong());
            data(body(put("/api/concerts/auto-move-past"),Map.of("currentDate","2026-10-31")));
            for (int i=0;i<ids.size();i++) mvc.perform(get("/api/concerts/"+ids.get(i))).andExpect(jsonPath("$.data.isPast").value(i==0));
            data(put("/api/concerts/"+ids.get(0)+"/move-to-upcoming"));
            mvc.perform(get("/api/concerts/"+ids.get(0))).andExpect(jsonPath("$.data.isPast").value(false));
            data(put("/api/concerts/"+ids.get(2)+"/move-to-past"));
            mvc.perform(get("/api/concerts/"+ids.get(2))).andExpect(jsonPath("$.data.isPast").value(true));
        } finally { for(Long id:ids) data(delete("/api/concerts/"+id)); }
    }
    @Test void photoUploadUpdateAndGroupCascade() throws Exception {
        long id=data(body(post("/api/media/photos/groups"),Map.of("title","Upload contract"))).path("id").asLong();
        byte[] png=Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jfsUAAAAASUVORK5CYII=");
        long photo=0;
        try {
            JsonNode photos=data(multipart("/api/media/photos/groups/"+id+"/photos")
                .file(new MockMultipartFile("files","one.png","image/png",png)).file(new MockMultipartFile("files","two.png","image/png",png)));
            assertEquals(2,photos.size()); photo=photos.get(0).path("id").asLong();
            data(body(put("/api/media/photos/"+photo),Map.of("altText","Portrait")));
            mvc.perform(get("/api/media/photos/"+photo)).andExpect(jsonPath("$.data.altText").value("Portrait"));
            data(delete("/api/media/photos/"+photos.get(1).path("id").asLong()));
            mvc.perform(get("/api/media/photos/groups/"+id+"/photos")).andExpect(jsonPath("$.data.length()").value(1));
        } finally { data(delete("/api/media/photos/groups/"+id)); }
        mvc.perform(get("/api/media/photos/"+photo)).andExpect(status().isNotFound());
    }
    @Test void badAndOversizedUploadsAreRejectedBeforePersistence() throws Exception {
        for (var file : List.of(
            new MockMultipartFile("file","empty.png","image/png",new byte[0]),
            new MockMultipartFile("file","script.html","text/html","<script>alert(1)</script>".getBytes()),
            new MockMultipartFile("file","broken.png","image/png",new byte[]{1,2,3}))) {
            mvc.perform(multipart("/api/upload/image").file(file).with(user("admin").roles("ADMIN"))).andExpect(status().isBadRequest());
        }
        Object previous=org.springframework.test.util.ReflectionTestUtils.getField(uploads,"uploadMaxSize");
        try {
            org.springframework.test.util.ReflectionTestUtils.setField(uploads,"uploadMaxSize","2B");
            mvc.perform(multipart("/api/upload/file").file(new MockMultipartFile("file","rider.pdf","application/pdf",new byte[]{1,2,3}))
                .with(user("admin").roles("ADMIN"))).andExpect(status().isBadRequest());
        } finally { org.springframework.test.util.ReflectionTestUtils.setField(uploads,"uploadMaxSize",previous); }
    }
    @Test void filtersAndMalformedParametersHavePredictableResults() throws Exception {
        var first=data(body(post("/api/projects"),Map.of("title","Search Alpha","urlSlug","search-alpha","location","Berlin","premiereDate","2026-01-01")));
        var second=data(body(post("/api/projects"),Map.of("title","Search Beta","urlSlug","search-beta","location","Seoul","premiereDate","2026-02-01")));
        try {
            mvc.perform(get("/api/projects").param("title","Alpha").param("location","Berlin")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1)).andExpect(jsonPath("$.data[0].id").value(first.path("id").asLong()));
            mvc.perform(get("/api/projects").param("startDate","2026-02-01").param("endDate","2026-02-01"))
                .andExpect(jsonPath("$.data.length()").value(1)).andExpect(jsonPath("$.data[0].id").value(second.path("id").asLong()));
            mvc.perform(get("/api/projects").param("title","not-present")).andExpect(jsonPath("$.data.length()").value(0));
            mvc.perform(get("/api/projects").param("startDate","not-a-date")).andExpect(status().isBadRequest());
            mvc.perform(get("/api/projects/not-an-id")).andExpect(status().isBadRequest());
        } finally { data(delete("/api/projects/"+first.path("id").asLong())); data(delete("/api/projects/"+second.path("id").asLong())); }
    }
}
