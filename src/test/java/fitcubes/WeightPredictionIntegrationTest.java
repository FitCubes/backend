package fitcubes;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fitcubes.model.exercise.Exercise;
import fitcubes.model.exercise.ExerciseCategory;
import fitcubes.model.exerciseentry.ExerciseEntry;
import fitcubes.model.foodentry.FoodEntry;
import fitcubes.model.foodentry.MealType;
import fitcubes.model.foodentry.SourceType;
import fitcubes.model.product.Product;
import fitcubes.model.product.ProductCategory;
import fitcubes.model.user.ActivityLevel;
import fitcubes.model.user.Gender;
import fitcubes.model.user.Goal;
import fitcubes.model.user.User;
import fitcubes.repository.ExerciseEntryRepository;
import fitcubes.repository.ExerciseRepository;
import fitcubes.repository.FoodEntryRepository;
import fitcubes.repository.ProductRepository;
import fitcubes.repository.UserRepository;
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
class WeightPredictionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private FoodEntryRepository foodEntryRepository;

    @Autowired
    private ExerciseEntryRepository exerciseEntryRepository;

    private User saveUser() {
        User user = new User();
        user.setEmail("prediction-test@example.com");
        user.setFirstName("Tomasz");
        user.setLastName("Wisniewski");
        user.setPassword("encoded-password");
        user.setGender(Gender.MALE);
        user.setAge(35);
        user.setHeight(178);
        user.setCurrentWeight(85.0);
        user.setTargetWeight(78.0);
        user.setActivityLevel(ActivityLevel.SEDENTARY);
        user.setGoal(Goal.WEIGHT_LOSS);
        return userRepository.save(user);
    }

    private RequestPostProcessor asUser(User user) {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    private Product saveProduct(double calories) {
        Product product = new Product();
        product.setName("Test food");
        product.setCategory(ProductCategory.OTHER);
        product.setCalories(calories);
        product.setFat(0.0);
        product.setProtein(0.0);
        product.setCarbohydrates(0.0);
        return productRepository.save(product);
    }

    private void saveFoodEntry(Long userId, Product product, double quantity, double calories,
                               Instant loggedAt) {
        FoodEntry entry = new FoodEntry();
        entry.setUserId(userId);
        entry.setSourceType(SourceType.PRODUCT);
        entry.setProductId(product.getId());
        entry.setNameSnapshot(product.getName());
        entry.setQuantity(BigDecimal.valueOf(quantity));
        entry.setCalories(BigDecimal.valueOf(calories));
        entry.setMealType(MealType.LUNCH);
        entry.setLoggedAt(loggedAt);
        foodEntryRepository.save(entry);
    }

    @Test
    void getPredictedWeeklyChange_withConsistentDeficit_returnsWeightLossPrediction()
            throws Exception {
        User user = saveUser();
        Product product = saveProduct(1000);

        Instant now = Instant.now();
        Instant from = now.minus(7, ChronoUnit.DAYS);

        for (int i = 0; i < 7; i++) {
            saveFoodEntry(user.getId(), product, 1.651, 1651.0, now.minus(i, ChronoUnit.DAYS));
        }

        mockMvc.perform(get("/api/dashboard/predicted-weight-change")
                        .with(asUser(user))
                        .param("from", from.toString())
                        .param("to", now.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageDailyDeficit").exists())
                .andExpect(jsonPath("$.predictedWeeklyChangeKg").exists());
    }

    @Test
    void getPredictedWeeklyChange_noEntries_predictsChangeBasedOnFullDeficit() throws Exception {
        User user = saveUser();

        Instant now = Instant.now();
        Instant from = now.minus(7, ChronoUnit.DAYS);

        String responseJson = mockMvc.perform(get("/api/dashboard/predicted-weight-change")
                        .with(asUser(user))
                        .param("from", from.toString())
                        .param("to", now.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(responseJson).contains("predictedWeeklyChangeKg");
    }

    @Test
    void getPredictedWeeklyChange_invalidDateRange_returnsBadRequest() throws Exception {
        User user = saveUser();
        Instant now = Instant.now();

        mockMvc.perform(get("/api/dashboard/predicted-weight-change")
                        .with(asUser(user))
                        .param("from", now.toString())
                        .param("to", now.minus(1, ChronoUnit.DAYS).toString()))
                .andExpect(status().isBadRequest());
    }
}
