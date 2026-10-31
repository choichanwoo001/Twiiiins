package com.twiiiins.newsletter;
import org.junit.jupiter.api.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.util.concurrent.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("mysql") @Testcontainers
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class MySqlNewsletterTest extends NewsletterIntegrationTest {
    @Container static final MySQLContainer<?> mysql=new MySQLContainer<>("mysql:8.0.43");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.jpa.hibernate.ddl-auto",()->"create");
        r.add("spring.datasource.url",mysql::getJdbcUrl); r.add("spring.datasource.username",mysql::getUsername);
        r.add("spring.datasource.password",mysql::getPassword); r.add("spring.datasource.driver-class-name",()->"com.mysql.cj.jdbc.Driver");
        r.add("spring.jpa.database-platform",()->"org.hibernate.dialect.MySQLDialect");
        r.add("spring.jpa.properties.hibernate.dialect",()->"org.hibernate.dialect.MySQLDialect");
        r.add("app.file.upload-dir",()->"build/mysql-newsletter-uploads");
    }
    @Test void concurrentSubscriptionCreatesOnlyOneWelcome() throws Exception {
        var pool=Executors.newFixedThreadPool(4); var ready=new CountDownLatch(4); var start=new CountDownLatch(1);
        try {
            List<Future<?>> tasks=new ArrayList<>();
            for(int i=0;i<4;i++) tasks.add(pool.submit(()-> { ready.countDown(); try { start.await(); } catch(InterruptedException e) { throw new RuntimeException(e); } subscriptions.subscribe("concurrent@example.com","en"); }));
            assertTrue(ready.await(10,TimeUnit.SECONDS)); start.countDown();
            for(var task:tasks) task.get(30,TimeUnit.SECONDS);
            assertEquals(1,subscribers.count()); assertEquals(1,deliveries.count());
        } finally { start.countDown(); pool.shutdownNow(); }
    }
}
