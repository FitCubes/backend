package fitcubes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import fitcubes.model.weightlog.WeightLog;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WeightLogRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private WeightLogRepository weightLogRepository;

    private static final Long USER_ID = 1L;

    private WeightLog buildLog(BigDecimal weight, Instant loggedAt) {
        WeightLog log = new WeightLog();
        log.setUserId(USER_ID);
        log.setWeight(weight);
        log.setLoggedAt(loggedAt);
        return log;
    }

    @Test
    void findLatestByUserId_returnsMostRecentLog() {
        Instant now = Instant.now();

        weightLogRepository.save(buildLog(BigDecimal.valueOf(80), now.minus(10, ChronoUnit.DAYS)));
        weightLogRepository.save(buildLog(BigDecimal.valueOf(78), now.minus(5, ChronoUnit.DAYS)));
        weightLogRepository.save(buildLog(BigDecimal.valueOf(76), now));

        Optional<WeightLog> result = weightLogRepository.findLatestByUserId(USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getWeight()).isEqualByComparingTo(BigDecimal.valueOf(76));
    }

    @Test
    void findLatestByUserId_noLogs_returnsEmpty() {
        Optional<WeightLog> result = weightLogRepository.findLatestByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findLatestByUserId_differentUser_notReturned() {
        Instant now = Instant.now();
        weightLogRepository.save(buildLog(BigDecimal.valueOf(80), now));

        Optional<WeightLog> result = weightLogRepository.findLatestByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findEarliestByUserId_returnsOldestLog() {
        Instant now = Instant.now();

        weightLogRepository.save(buildLog(BigDecimal.valueOf(85), now.minus(20, ChronoUnit.DAYS)));
        weightLogRepository.save(buildLog(BigDecimal.valueOf(82), now.minus(10, ChronoUnit.DAYS)));
        weightLogRepository.save(buildLog(BigDecimal.valueOf(80), now));

        Optional<WeightLog> result = weightLogRepository.findEarliestByUserId(USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getWeight()).isEqualByComparingTo(BigDecimal.valueOf(85));
    }

    @Test
    void findEarliestByUserId_noLogs_returnsEmpty() {
        Optional<WeightLog> result = weightLogRepository.findEarliestByUserId(999L);

        assertThat(result).isEmpty();
    }
}
