package fitcubes;

import fitcubes.config.TestcontainersConfiguration;
import fitcubes.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
class ApplicationContextTest {

    @Test
    void contextLoads() {
    }
}