package fitcubes.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class DailySummaryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        user.setEmail("dashboard-test@example.com");
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setPassword("encoded-password");
        user.setGender(Gender.MALE);
        user.setAge(30);
        user.setHeight(180);
        user.setCurrentWeight(80.0);
        user.setTargetWeight(75.0);
        user.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE);
        user.setGoal(Goal.MAINTENANCE);
        return userRepository.save(user);
    }

    private RequestPostProcessor asUser(User user) {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    private Product saveProduct() {
        Product product = new Product();
        product.setName("Chicken breast");
        product.setCategory(ProductCategory.MEAT_AND_POULTRY);
        product.setCalories(165.0);
        product.setFat(3.6);
        product.setProtein(31.0);
        product.setCarbohydrates(0.0);
        return productRepository.save(product);
    }

    private Exercise saveExercise() {
        Exercise exercise = new Exercise();
        exercise.setName("Running");
        exercise.setCategory(ExerciseCategory.CARDIO);
        exercise.setPrimaryMuscles("Legs");
        exercise.setMet(BigDecimal.valueOf(8));
        return exerciseRepository.save(exercise);
    }

    @Test
    void getDailySummary_withFoodAndExerciseEntries_returnsCorrectBalance() throws Exception {
        User user = saveUser();
        Product product = saveProduct();
        Exercise exercise = saveExercise();

        Instant loggedAt = Instant.now();

        FoodEntry foodEntry = new FoodEntry();
        foodEntry.setUserId(user.getId());
        foodEntry.setSourceType(SourceType.PRODUCT);
        foodEntry.setProductId(product.getId());
        foodEntry.setNameSnapshot(product.getName());
        foodEntry.setQuantity(BigDecimal.valueOf(2));
        foodEntry.setCalories(BigDecimal.valueOf(330));
        foodEntry.setMealType(MealType.LUNCH);
        foodEntry.setLoggedAt(loggedAt);
        foodEntryRepository.save(foodEntry);

        ExerciseEntry exerciseEntry = new ExerciseEntry();
        exerciseEntry.setUserId(user.getId());
        exerciseEntry.setExerciseId(exercise.getId());
        exerciseEntry.setNameSnapshot(exercise.getName());
        exerciseEntry.setDurationMinutes(BigDecimal.valueOf(30));
        exerciseEntry.setCaloriesBurned(BigDecimal.valueOf(294));
        exerciseEntry.setLoggedAt(loggedAt);
        exerciseEntryRepository.save(exerciseEntry);

        Instant from = loggedAt.minusSeconds(3600);
        Instant to = loggedAt.plusSeconds(3600);

        mockMvc.perform(get("/api/dashboard/daily-summary")
                        .with(asUser(user))
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumed").value(330.0))
                .andExpect(jsonPath("$.burned").value(294.0));
    }

    @Test
    void getDailySummary_noEntries_remainingEqualsTarget() throws Exception {
        User user = saveUser();

        Instant now = Instant.now();
        Instant from = now.minusSeconds(3600);
        Instant to = now.plusSeconds(3600);

        String responseJson = mockMvc.perform(get("/api/dashboard/daily-summary")
                        .with(asUser(user))
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumed").value(0))
                .andExpect(jsonPath("$.burned").value(0))
                .andReturn().getResponse().getContentAsString();

        double target = objectMapper.readTree(responseJson).get("targetCalories").asDouble();
        double remaining = objectMapper.readTree(responseJson).get("remaining").asDouble();

        assertThat(remaining).isEqualTo(target);
    }
}
