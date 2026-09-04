package fitcubes.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fitcubes.dto.exerciseentry.ExerciseEntryRequestDto;
import fitcubes.dto.weightlog.WeightLogRequestDto;
import fitcubes.model.exercise.Exercise;
import fitcubes.model.exercise.ExerciseCategory;
import fitcubes.model.exerciseentry.ExerciseEntry;
import fitcubes.model.user.User;
import fitcubes.model.weightlog.WeightLog;
import fitcubes.repository.ExerciseEntryRepository;
import fitcubes.repository.ExerciseRepository;
import fitcubes.repository.WeightLogRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
class ExerciseEntryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private ExerciseEntryRepository exerciseEntryRepository;

    @Autowired
    private WeightLogRepository weightLogRepository;

    private static final Long USER_ID = 42L;
    private static final Long OTHER_USER_ID = 999L;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
    }

    private RequestPostProcessor asUser() {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        testUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    private Exercise saveExercise(String name, double met) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setCategory(ExerciseCategory.CARDIO);
        exercise.setPrimaryMuscles("Legs");
        exercise.setMet(BigDecimal.valueOf(met));
        return exerciseRepository.save(exercise);
    }

    private void saveWeightLog(Long userId, double weight) {
        WeightLog log = new WeightLog();
        log.setUserId(userId);
        log.setWeight(BigDecimal.valueOf(weight));
        log.setLoggedAt(Instant.now());
        weightLogRepository.save(log);
    }

    @Test
    void addExerciseEntry_withExistingWeightLog_calculatesCorrectCalories() throws Exception {
        Exercise running = saveExercise("Running", 8);
        saveWeightLog(USER_ID, 70);

        ExerciseEntryRequestDto request = new ExerciseEntryRequestDto(
                running.getId(), BigDecimal.valueOf(30), Instant.now());

        String responseJson = mockMvc.perform(post("/api/exercise-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caloriesBurned").value(294.0))
                .andExpect(jsonPath("$.nameSnapshot").value("Running"))
                .andReturn().getResponse().getContentAsString();

        Long entryId = objectMapper.readTree(responseJson).get("id").asLong();

        ExerciseEntry persisted = exerciseEntryRepository
                .findByIdAndUserId(entryId, USER_ID).orElseThrow();
        assertThat(persisted.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(294.00));
        assertThat(persisted.getExerciseId()).isEqualTo(running.getId());
    }

    @Test
    void addWeightLog_thenAddExerciseEntry_usesNewlyLoggedWeight() throws Exception {
        Exercise cycling = saveExercise("Cycling", 6);

        WeightLogRequestDto weightRequest = new WeightLogRequestDto(
                BigDecimal.valueOf(75), Instant.now());

        mockMvc.perform(post("/api/weight-logs")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(weightRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weight").value(75));

        ExerciseEntryRequestDto exerciseRequest = new ExerciseEntryRequestDto(
                cycling.getId(), BigDecimal.valueOf(45), Instant.now());

        mockMvc.perform(post("/api/exercise-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(exerciseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caloriesBurned").value(354.38));
    }

    @Test
    void addExerciseEntry_nonExistentExercise_returns404() throws Exception {
        ExerciseEntryRequestDto request = new ExerciseEntryRequestDto(
                999999L, BigDecimal.valueOf(30), Instant.now());

        mockMvc.perform(post("/api/exercise-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateExerciseEntry_changedDuration_recalculatesCalories() throws Exception {
        Exercise running = saveExercise("Running", 8);
        saveWeightLog(USER_ID, 70);

        ExerciseEntryRequestDto createRequest = new ExerciseEntryRequestDto(
                running.getId(), BigDecimal.valueOf(30), Instant.now());

        String createResponse = mockMvc.perform(post("/api/exercise-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long entryId = objectMapper.readTree(createResponse).get("id").asLong();

        ExerciseEntryRequestDto updateRequest = new ExerciseEntryRequestDto(
                running.getId(), BigDecimal.valueOf(60), Instant.now());

        mockMvc.perform(patch("/api/exercise-entries/" + entryId)
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caloriesBurned").value(588.0));

        ExerciseEntry persisted = exerciseEntryRepository
                .findByIdAndUserId(entryId, USER_ID).orElseThrow();
        assertThat(persisted.getDurationMinutes()).isEqualByComparingTo(BigDecimal.valueOf(60));
    }

    @Test
    void deleteExerciseEntry_removesFromDatabase() throws Exception {
        Exercise yoga = saveExercise("Yoga", 2.5);
        saveWeightLog(USER_ID, 65);

        ExerciseEntryRequestDto request = new ExerciseEntryRequestDto(
                yoga.getId(), BigDecimal.valueOf(20), Instant.now());

        String createResponse = mockMvc.perform(post("/api/exercise-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long entryId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/api/exercise-entries/" + entryId)
                        .with(csrf())
                        .with(asUser()))
                .andExpect(status().isNoContent());

        assertThat(exerciseEntryRepository.findByIdAndUserId(entryId, USER_ID)).isEmpty();
    }

    @Test
    void getExerciseEntry_belongingToOtherUser_returns404() throws Exception {
        Exercise running = saveExercise("Running", 8);
        saveWeightLog(USER_ID, 70);

        ExerciseEntryRequestDto request = new ExerciseEntryRequestDto(
                running.getId(), BigDecimal.valueOf(30), Instant.now());

        String createResponse = mockMvc.perform(post("/api/exercise-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long entryId = objectMapper.readTree(createResponse).get("id").asLong();

        User otherUser = new User();
        otherUser.setId(OTHER_USER_ID);
        RequestPostProcessor asOtherUser = authentication(
                new UsernamePasswordAuthenticationToken(
                        otherUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        mockMvc.perform(get("/api/exercise-entries/" + entryId).with(asOtherUser))
                .andExpect(status().isNotFound());
    }
}