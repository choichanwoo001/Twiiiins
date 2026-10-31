package com.twiiiins.translation;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.*;
@Tag("mysql") @Testcontainers
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class MySqlTranslationTest extends TranslationIntegrationTest {
    @Container static final MySQLContainer<?> mysql=new MySQLContainer<>("mysql:8.0.43");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.jpa.hibernate.ddl-auto",()->"create");
        r.add("spring.datasource.url",mysql::getJdbcUrl); r.add("spring.datasource.username",mysql::getUsername);
        r.add("spring.datasource.password",mysql::getPassword); r.add("spring.datasource.driver-class-name",()->"com.mysql.cj.jdbc.Driver");
        r.add("spring.jpa.database-platform",()->"org.hibernate.dialect.MySQLDialect");
        r.add("spring.jpa.properties.hibernate.dialect",()->"org.hibernate.dialect.MySQLDialect");
    }
    @org.junit.jupiter.api.Test void deleteSurvivesAWorkerUpdatingAnEarlierReadVersion() throws Exception {
        var project=project("Concurrent delete");
        org.mockito.Mockito.when(gemini.translate(org.mockito.ArgumentMatchers.any()))
            .thenReturn(new GeminiTranslator.Result("en","Concurrent delete","Gleichzeitig löschen"));
        var pool=java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            new org.springframework.transaction.support.TransactionTemplate(transactions).executeWithoutResult(tx->{
                // Establish an old repeatable-read snapshot before the worker commits.
                var earlier=rows.findByKindAndContentId("projects",project.getId()).get(0);
                try { pool.submit(()->worker.process(earlier.getId())).get(20,java.util.concurrent.TimeUnit.SECONDS); }
                catch(Exception e) { throw new RuntimeException(e); }
                projects.deleteProject(project.getId());
            });
            org.junit.jupiter.api.Assertions.assertFalse(projectRepository.existsById(project.getId()));
            org.junit.jupiter.api.Assertions.assertTrue(rows.findByKindAndContentId("projects",project.getId()).isEmpty());
        } finally { pool.shutdownNow(); }
    }
}
