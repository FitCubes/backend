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
import fitcubes.dto.foodentry.FoodEntryRequestDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.foodentry.MealType;
import fitcubes.model.foodentry.SourceType;
import fitcubes.model.user.User;
import fitcubes.security.JwtUtil;
import fitcubes.security.TokenBlacklistService;
import fitcubes.service.FoodEntryService;
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

@WebMvcTest(FoodEntryController.class)
class FoodEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FoodEntryService foodEntryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(42L);
    }

    private RequestPostProcessor asUser() {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        testUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    private FoodEntryRequestDto productRequest() {
        return new FoodEntryRequestDto(
                SourceType.PRODUCT, 1L, null, null, null,
                null, null, null, null,
                BigDecimal.valueOf(2), MealType.BREAKFAST, Instant.now()
        );
    }

    private FoodEntryResponseDto responseDto() {
        return new FoodEntryResponseDto(
                10L, SourceType.PRODUCT, 1L, null, "Egg",
                BigDecimal.valueOf(2), BigDecimal.valueOf(156), BigDecimal.valueOf(12),
                BigDecimal.ZERO, BigDecimal.valueOf(10), MealType.BREAKFAST, Instant.now()
        );
    }

    @Test
    void addFoodEntry_returnsCreated() throws Exception {
        when(foodEntryService.addFoodEntry(eq(42L), any())).thenReturn(responseDto());

        mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.calories").value(156));
    }

    @Test
    void addFoodEntry_missingQuantity_returnsBadRequest() throws Exception {
        FoodEntryRequestDto invalid = new FoodEntryRequestDto(
                SourceType.PRODUCT, 1L, null, null, null,
                null, null, null, null,
                null, MealType.BREAKFAST, Instant.now()
        );

        mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFoodEntry_returnsOk() throws Exception {
        when(foodEntryService.updateFoodEntry(eq(42L), eq(10L), any())).thenReturn(responseDto());

        mockMvc.perform(patch("/api/food-entries/10")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void updateFoodEntry_notFound_returns404() throws Exception {
        when(foodEntryService.updateFoodEntry(eq(42L), eq(999L), any()))
                .thenThrow(new EntityNotFoundException("Food entry not found: 999"));

        mockMvc.perform(patch("/api/food-entries/999")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFoodEntry_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/food-entries/10")
                        .with(csrf())
                        .with(asUser()))
                .andExpect(status().isNoContent());

        verify(foodEntryService).deleteFoodEntry(42L, 10L);
    }

    @Test
    void getFoodEntry_returnsOk() throws Exception {
        when(foodEntryService.getFoodEntry(42L, 10L)).thenReturn(responseDto());

        mockMvc.perform(get("/api/food-entries/10").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameSnapshot").value("Egg"));
    }

    @Test
    void getFoodEntries_returnsPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<FoodEntryResponseDto> page = new PageImpl<>(List.of(responseDto()), pageable, 1);

        when(foodEntryService.getFoodEntries(eq(42L), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/food-entries")
                        .with(asUser())
                        .param("from", "2026-08-01T00:00:00Z")
                        .param("to", "2026-08-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10));
    }

    @Test
    void unauthenticated_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/food-entries")
                        .param("from", "2026-08-01T00:00:00Z")
                        .param("to", "2026-08-31T23:59:59Z"))
                .andExpect(status().is4xxClientError());
    }
}