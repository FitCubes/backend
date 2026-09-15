package fitcubes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fitcubes.dto.exercise.ExerciseDto;
import fitcubes.model.exercise.ExerciseCategory;
import fitcubes.security.CustomAuthenticationEntryPoint;
import fitcubes.security.JwtUtil;
import fitcubes.security.TokenBlacklistService;
import fitcubes.service.exercise.ExerciseService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    private static final Long EXERCISE_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseService exerciseService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    private ExerciseDto exerciseDto;

    @BeforeEach
    void setUp() {
        exerciseDto = new ExerciseDto(
                EXERCISE_ID, "Running", ExerciseCategory.CARDIO, "Legs", BigDecimal.valueOf(8));
    }

    private RequestPostProcessor asUser() {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Test
    void getAllActivities_returnsPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ExerciseDto> page = new PageImpl<>(List.of(exerciseDto), pageable, 1);

        given(exerciseService.getAllExercises(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/v1/activities").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(EXERCISE_ID))
                .andExpect(jsonPath("$.content[0].name").value("Running"));
    }

    @Test
    void searchActivities_returnsMatchingPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ExerciseDto> page = new PageImpl<>(List.of(exerciseDto), pageable, 1);

        given(exerciseService.searchExercises(eq("run"), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/v1/activities/search")
                        .param("query", "run")
                        .with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Running"));
    }

    @Test
    void searchActivities_missingQueryParam_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/activities/search").with(asUser()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllActivities_unauthenticated_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/v1/activities"))
                .andExpect(status().is4xxClientError());
    }
}
