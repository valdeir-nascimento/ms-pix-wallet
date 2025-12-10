package br.com.pix.wallet;

import br.com.pix.wallet.infrastructure.config.WebServerConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ActiveProfiles("test-e2e")
@TestPropertySource(properties = "pix.wallet.security.enabled=false")
@SpringBootTest(classes = WebServerConfig.class)
@ExtendWith(PostgreSQLCleanUpExtension.class)
@AutoConfigureMockMvc
public @interface E2ETest {

}
