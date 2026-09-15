package fitcubes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fitcubes.dto.diary.DiaryExerciseEntryDto;
import fitcubes.dto.diary.DiaryFoodEntryDto;
import fitcubes.dto.diary.DiaryResponseDto;
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import fitcubes.model.foodentry.MealType;
import fitcubes.model.foodentry.SourceType;
import fitcubes.model.user.User;
import fitcubes.security.CustomAuthenticationEntryPoint;
import fitcubes.security.JwtUtil;
import fitcubes.security.TokenBlacklistService;
import fitcubes.service.diary.DiaryService;
import fitcubes.service.exerciseentry.ExerciseEntryService;
import fitcubes.service.foodentry.FoodEntryService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(DiaryController.class)
class DiaryControllerTest {

    private static final Long USER_ID = 42L;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 14);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiaryService diaryService;

    @MockitoBean
    private FoodEntryService foodEntryService;

    @MockitoBean
    private ExerciseEntryService exerciseEntryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

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

    @Test
    void getDiary_returnsAggregatedEntries() throws Exception {
        DiaryFoodEntryDto food = new DiaryFoodEntryDto(
                1L, "Chicken Breast", BigDecimal.valueOf(165), BigDecimal.valueOf(31),
                BigDecimal.ZERO, BigDecimal.valueOf(3.6), BigDecimal.valueOf(100), "lunch"
        );
        DiaryExerciseEntryDto exercise = new DiaryExerciseEntryDto(
                2L, "Running", BigDecimal.valueOf(320), BigDecimal.valueOf(30)
        );
        DiaryResponseDto response = new DiaryResponseDto(DATE, List.of(food), List.of(exercise));

        when(diaryService.getDiary(USER_ID, DATE)).thenReturn(response);

        mockMvc.perform(get("/api/v1/diary/{date}", DATE).with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-09-14"))
                .andExpect(jsonPath("$.foodEntries[0].name").value("Chicken Breast"))
                .andExpect(jsonPath("$.foodEntries[0].mealType").value("lunch"))
                .andExpect(jsonPath("$.exerciseEntries[0].name").value("Running"));
    }

    @Test
    void getDiary_unauthenticated_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/v1/diary/{date}", DATE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void addFoodEntry_returnsCreated() throws Exception {
        FoodEntryResponseDto saved = new FoodEntryResponseDto(
                1L, SourceType.PRODUCT, 5L, null, "Egg",
                BigDecimal.valueOf(200), BigDecimal.valueOf(310), BigDecimal.valueOf(26),
                BigDecimal.valueOf(2.2), BigDecimal.valueOf(22), MealType.BREAKFAST, Instant.now()
        );
        when(foodEntryService.addFoodEntry(eq(USER_ID), any())).thenReturn(saved);

        String requestBody = """
                {
                    "sourceType": "PRODUCT",
                    "productId": 5,
                    "quantity": 200,
                    "mealType": "BREAKFAST",
                    "loggedAt": "2026-09-14T10:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/diary/{date}/food", DATE)
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Egg"))
                .andExpect(jsonPath("$.mealType").value("breakfast"))
                .andExpect(jsonPath("$.weightGrams").value(200));
    }

    @Test
    void deleteFoodEntry_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/diary/{date}/food/{id}", DATE, 1L)
                        .with(csrf())
                        .with(asUser()))
                .andExpect(status().isNoContent());

        verify(foodEntryService).deleteFoodEntry(USER_ID, 1L);
    }

    @Test
    void addExerciseEntry_returnsCreated() throws Exception {
        ExerciseEntryResponseDto saved = new ExerciseEntryResponseDto(
                2L, 7L, "Running", BigDecimal.valueOf(30), BigDecimal.valueOf(320), Instant.now()
        );
        when(exerciseEntryService.addExerciseEntry(eq(USER_ID), any())).thenReturn(saved);

        String requestBody = """
                {
                    "exerciseId": 7,
                    "durationMinutes": 30,
                    "loggedAt": "2026-09-14T10:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/diary/{date}/exercise", DATE)
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Running"))
                .andExpect(jsonPath("$.caloriesBurned").value(320));
    }

    @Test
    void deleteExerciseEntry_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/diary/{date}/exercise/{id}", DATE, 2L)
                        .with(csrf())
                        .with(asUser()))
                .andExpect(status().isNoContent());

        verify(exerciseEntryService).deleteExerciseEntry(USER_ID, 2L);
    }
}
