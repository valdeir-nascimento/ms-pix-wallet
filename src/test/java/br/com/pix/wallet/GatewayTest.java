package br.com.pix.wallet;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ActiveProfiles("test-integration")
@ComponentScan(
    basePackages = "br.com.pix.wallet",
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Gateway.*")
    }
)
@DataJpaTest
@ExtendWith(PostgreSQLCleanUpExtension.class)
public @interface GatewayTest {
}
