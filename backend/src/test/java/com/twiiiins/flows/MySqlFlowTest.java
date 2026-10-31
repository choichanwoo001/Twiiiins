package com.twiiiins.flows;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("mysql") @Testcontainers
@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=create", "app.newsletter.enabled=false", "app.translation.enabled=false", "app.file.upload-dir=build/mysql-test-uploads"})
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
class MySqlFlowTest extends CmsApiContract {
    @Container static final MySQLContainer<?> mysql=new MySQLContainer<>("mysql:8.0.43")
        .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",mysql::getJdbcUrl); r.add("spring.datasource.username",mysql::getUsername);
        r.add("spring.datasource.password",mysql::getPassword);
    }
}
