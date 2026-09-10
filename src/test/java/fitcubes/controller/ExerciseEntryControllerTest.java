package fitcubes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.user.User;
import fitcubes.security.JwtUtil;
import fitcubes.security.TokenBlacklistService;
import fitcubes.service.exerciseentry.ExerciseEntryService;
import java.math.BigDecimal;
import java.time.Instant;
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

@WebMvcTest(ExerciseEntryController.class)
class ExerciseEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExerciseEntryService exerciseEntryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private User testUser;

    private static final Long USER_ID = 42L;
    private static final Long ENTRY_ID = 10L;

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

    private ExerciseEntryRequestDto request() {
        return new ExerciseEntryRequestDto(1L, BigDecimal.valueOf(30), Instant.now());
    }

    private ExerciseEntryResponseDto responseDto() {
        return new ExerciseEntryResponseDto(
                ENTRY_ID, 1L, "Running", BigDecimal.valueOf(30),
                BigDecimal.valueOf(294), Instant.now());
    }

    @Test
    void addExerciseEntry_returnsCreated() throws Exception {
        when(exerciseEntryService.addExerciseEntry(eq(USER_ID), any())).thenReturn(responseDto());

        mockMvc.perform(post("/api/v1/exercise-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caloriesBurned").value(294))
                .andExpect(jsonPath("$.nameSnapshot").value("Running"));
    }

    @Test
    void addExerciseEntry_missingExerciseId_returnsBadRequest() throws Exception {
        ExerciseEntryRequestDto invalid = new ExerciseEntryRequestDto(
                null, BigDecimal.valueOf(30), Instant.now());

        mockMvc.perform(post("/api/v1/exercise-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addExerciseEntry_negativeDuration_returnsBadRequest() throws Exception {
        ExerciseEntryRequestDto invalid = new ExerciseEntryRequestDto(
                1L, BigDecimal.valueOf(-5), Instant.now());

        mockMvc.perform(post("/api/v1/exercise-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExerciseEntry_returnsOk() throws Exception {
        when(exerciseEntryService.updateExerciseEntry(eq(USER_ID), eq(ENTRY_ID), any()))
                .thenReturn(responseDto());

        mockMvc.perform(patch("/api/v1/exercise-entries/" + ENTRY_ID)
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ENTRY_ID));
    }

    @Test
    void updateExerciseEntry_notFound_returns404() throws Exception {
        when(exerciseEntryService.updateExerciseEntry(eq(USER_ID), eq(999L), any()))
                .thenThrow(new EntityNotFoundException("Exercise entry not found: 999"));

        mockMvc.perform(patch("/api/v1/exercise-entries/999")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExerciseEntry_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/exercise-entries/" + ENTRY_ID)
                        .with(csrf())
                        .with(asUser()))
                .andExpect(status().isNoContent());

        verify(exerciseEntryService).deleteExerciseEntry(USER_ID, ENTRY_ID);
    }

    @Test
    void getExerciseEntry_returnsOk() throws Exception {
        when(exerciseEntryService.getExerciseEntry(USER_ID, ENTRY_ID)).thenReturn(responseDto());

        mockMvc.perform(get("/api/v1/exercise-entries/" + ENTRY_ID).with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameSnapshot").value("Running"));
    }

    @Test
    void getExerciseEntries_returnsPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ExerciseEntryResponseDto> page = new PageImpl<>(List.of(responseDto()), pageable, 1);

        when(exerciseEntryService.getExerciseEntries(eq(USER_ID), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/exercise-entries")
                        .with(asUser())
                        .param("from", "2026-08-01T00:00:00Z")
                        .param("to", "2026-08-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ENTRY_ID));
    }

    @Test
    void unauthenticated_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/v1/exercise-entries")
                        .param("from", "2026-08-01T00:00:00Z")
                        .param("to", "2026-08-31T23:59:59Z"))
                .andExpect(status().is4xxClientError());
    }
}
