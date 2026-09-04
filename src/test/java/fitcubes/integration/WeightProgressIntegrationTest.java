package fitcubes.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fitcubes.model.user.ActivityLevel;
import fitcubes.model.user.Gender;
import fitcubes.model.user.Goal;
import fitcubes.model.user.User;
import fitcubes.model.weightlog.WeightLog;
import fitcubes.repository.UserRepository;
import fitcubes.repository.WeightLogRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class WeightProgressIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeightLogRepository weightLogRepository;

    private User saveUser() {
        User user = new User();
        user.setEmail("weight-progress-test@example.com");
        user.setFirstName("Anna");
        user.setLastName("Nowak");
        user.setPassword("encoded-password");
        user.setGender(Gender.FEMALE);
        user.setAge(28);
        user.setHeight(165);
        user.setCurrentWeight(70.0);
        user.setTargetWeight(62.0);
        user.setActivityLevel(ActivityLevel.LIGHTLY_ACTIVE);
        user.setGoal(Goal.WEIGHT_LOSS);
        return userRepository.save(user);
    }

    private RequestPostProcessor asUser(User user) {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    private void saveWeightLog(Long userId, double weight, Instant loggedAt) {
        WeightLog log = new WeightLog();
        log.setUserId(userId);
        log.setWeight(BigDecimal.valueOf(weight));
        log.setLoggedAt(loggedAt);
        weightLogRepository.save(log);
    }

    @Test
    void getWeightProgress_withHistory_returnsCorrectProgress() throws Exception {
        User user = saveUser();
        Instant now = Instant.now();

        saveWeightLog(user.getId(), 74.0, now.minus(30, ChronoUnit.DAYS));
        saveWeightLog(user.getId(), 72.0, now.minus(15, ChronoUnit.DAYS));
        saveWeightLog(user.getId(), 70.0, now);

        mockMvc.perform(get("/api/weight-logs/progress").with(asUser(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startingWeight").value(74.0))
                .andExpect(jsonPath("$.currentWeight").value(70.0))
                .andExpect(jsonPath("$.targetWeight").value(62.0))
                .andExpect(jsonPath("$.totalChange").value(-4.0))
                .andExpect(jsonPath("$.remainingToGoal").value(8.0));
    }

    @Test
    void getWeightProgress_noHistory_fallsBackToCurrentWeight() throws Exception {
        User user = saveUser();

        mockMvc.perform(get("/api/weight-logs/progress").with(asUser(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startingWeight").value(70.0))
                .andExpect(jsonPath("$.currentWeight").value(70.0))
                .andExpect(jsonPath("$.totalChange").value(0.0));
    }

    @Test
    void addWeightLog_thenGetProgress_reflectsNewEntry() throws Exception {
        User user = saveUser();
        Instant now = Instant.now();

        saveWeightLog(user.getId(), 74.0, now.minus(10, ChronoUnit.DAYS));

        String requestBody = String.format(
                "{\"weight\": 71.5, \"loggedAt\": \"%s\"}", now.toString());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/weight-logs")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.csrf())
                        .with(asUser(user))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/weight-logs/progress").with(asUser(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentWeight").value(71.5))
                .andExpect(jsonPath("$.totalChange").value(-2.5));
    }
}
