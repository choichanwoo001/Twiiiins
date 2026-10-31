package com.twiiiins.flows;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.junit.jupiter.api.AfterAll;
import java.nio.file.*;
import java.io.IOException;
import java.util.Comparator;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:cms;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop", "app.newsletter.enabled=false", "app.translation.enabled=false"
})
@AutoConfigureMockMvc
class CmsFlowTest extends CmsApiContract {
    static final Path uploads;
    static { try { uploads=Files.createTempDirectory("twiiiins-cms-test-"); } catch(IOException e) { throw new RuntimeException(e); } }
    @DynamicPropertySource static void storage(DynamicPropertyRegistry r) { r.add("app.file.upload-dir",uploads::toString); }
    @AfterAll static void cleanup() throws IOException { try(var paths=Files.walk(uploads)) { for(Path p:paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(p); } }
}
