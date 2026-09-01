package fitcubes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fitcubes.config.TestSecurityConfig;
import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.RecipeIngredientDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.recipe.RecipeCategory;
import fitcubes.security.JwtUtil;
import fitcubes.security.TokenBlacklistService;
import fitcubes.service.recipe.AdminRecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRecipeController.class)
@Import(TestSecurityConfig.class)
class AdminRecipeControllerTest {

    private static final String BASE_URL = "/api/admin/recipes";
    private static final Long RECIPE_ID = 5L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminRecipeService adminRecipeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private CreateRecipeDto createRecipeDto;
    private UpdateRecipeDto updateRecipeDto;
    private RecipeDto recipeDto;

    @BeforeEach
    void setUp() {
        RecipeIngredientDto ingredientDto = new RecipeIngredientDto(
                1L, 1L, "Oats", 100.0, 389.0, 16.9, 66.3, 6.9, 67.6
        );

        createRecipeDto = new CreateRecipeDto(
                "Oatmeal",
                RecipeCategory.BREAKFAST,
                "Healthy breakfast recipe",
                1,
                100.0,
                250.0,
                150.0,
                6.0,
                25.0,
                3.0,
                List.of(ingredientDto)
        );

        updateRecipeDto = new UpdateRecipeDto(
                "Protein Oatmeal",
                RecipeCategory.BREAKFAST,
                "Updated description",
                1,
                120.0,
                270.0,
                180.0,
                15.0,
                25.0,
                4.0,
                List.of(ingredientDto)
        );

        recipeDto = new RecipeDto(
                RECIPE_ID,
                100L,
                "Oatmeal",
                RecipeCategory.BREAKFAST,
                "Healthy breakfast recipe",
                1,
                100.0,
                250.0,
                150.0,
                6.0,
                25.0,
                3.0,
                24.0, // Wyliczone proteinCaloriesPer100g (6.0 * 4.0)
                List.of(ingredientDto)
        );
    }

    @Nested
    @DisplayName("createRecipe")
    class CreateRecipe {

        @Test
        @DisplayName("createRecipe_asAdmin_returnsCreatedRecipe")
        @WithMockUser(roles = "ADMIN")
        void createRecipe_asAdmin_returnsCreatedRecipe() throws Exception {
            given(adminRecipeService.save(createRecipeDto)).willReturn(recipeDto);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRecipeDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(RECIPE_ID))
                    .andExpect(jsonPath("$.name").value("Oatmeal"))
                    .andExpect(jsonPath("$.category").value("BREAKFAST"))
                    .andExpect(jsonPath("$.proteinCaloriesPer100g").value(24.0))
                    .andExpect(jsonPath("$.ingredients[0].name").value("Oats"));

            verify(adminRecipeService).save(createRecipeDto);
        }

        @Test
        @DisplayName("createRecipe_blankName_returnsBadRequest")
        @WithMockUser(roles = "ADMIN")
        void createRecipe_blankName_returnsBadRequest() throws Exception {
            CreateRecipeDto invalidDto = new CreateRecipeDto(
                    "   ",
                    createRecipeDto.category(),
                    createRecipeDto.description(),
                    createRecipeDto.servings(),
                    createRecipeDto.rawWeight(),
                    createRecipeDto.cookedWeight(),
                    createRecipeDto.caloriesPer100g(),
                    createRecipeDto.proteinPer100g(),
                    createRecipeDto.carbsPer100g(),
                    createRecipeDto.fatsPer100g(),
                    createRecipeDto.ingredients()
            );

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(adminRecipeService, never()).save(any());
        }

        @Test
        @DisplayName("createRecipe_emptyIngredients_returnsBadRequest")
        @WithMockUser(roles = "ADMIN")
        void createRecipe_emptyIngredients_returnsBadRequest() throws Exception {
            CreateRecipeDto invalidDto = new CreateRecipeDto(
                    createRecipeDto.name(),
                    createRecipeDto.category(),
                    createRecipeDto.description(),
                    createRecipeDto.servings(),
                    createRecipeDto.rawWeight(),
                    createRecipeDto.cookedWeight(),
                    createRecipeDto.caloriesPer100g(),
                    createRecipeDto.proteinPer100g(),
                    createRecipeDto.carbsPer100g(),
                    createRecipeDto.fatsPer100g(),
                    Collections.emptyList()
            );

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(adminRecipeService, never()).save(any());
        }

        @Test
        @DisplayName("createRecipe_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void createRecipe_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRecipeDto)))
                    .andExpect(status().isForbidden());

            verify(adminRecipeService, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllRecipes")
    class GetAllRecipes {

        @Test
        @DisplayName("getAllRecipes_asAdmin_returnsPage")
        @WithMockUser(roles = "ADMIN")
        void getAllRecipes_asAdmin_returnsPage() throws Exception {
            Page<RecipeDto> page = new PageImpl<>(List.of(recipeDto));
            given(adminRecipeService.getAllRecipes(any(Pageable.class))).willReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(RECIPE_ID))
                    .andExpect(jsonPath("$.content[0].name").value("Oatmeal"))
                    .andExpect(jsonPath("$.content[0].proteinCaloriesPer100g").value(24.0));
        }

        @Test
        @DisplayName("getAllRecipes_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void getAllRecipes_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());

            verify(adminRecipeService, never()).getAllRecipes(any());
        }
    }

    @Nested
    @DisplayName("getRecipeById")
    class GetRecipeById {

        @Test
        @DisplayName("getRecipeById_asAdmin_returnsRecipe")
        @WithMockUser(roles = "ADMIN")
        void getRecipeById_asAdmin_returnsRecipe() throws Exception {
            given(adminRecipeService.getRecipeById(RECIPE_ID)).willReturn(recipeDto);

            mockMvc.perform(get(BASE_URL + "/{recipeId}", RECIPE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RECIPE_ID))
                    .andExpect(jsonPath("$.name").value("Oatmeal"))
                    .andExpect(jsonPath("$.proteinCaloriesPer100g").value(24.0));
        }

        @Test
        @DisplayName("getRecipeById_notFound_returnsNotFound")
        @WithMockUser(roles = "ADMIN")
        void getRecipeById_notFound_returnsNotFound() throws Exception {
            given(adminRecipeService.getRecipeById(RECIPE_ID))
                    .willThrow(new EntityNotFoundException(
                            "Recipe with recipeId: " + RECIPE_ID + " not found"));

            mockMvc.perform(get(BASE_URL + "/{recipeId}", RECIPE_ID))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("getRecipeById_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void getRecipeById_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{recipeId}", RECIPE_ID))
                    .andExpect(status().isForbidden());

            verify(adminRecipeService, never()).getRecipeById(anyLong());
        }
    }

    @Nested
    @DisplayName("updateRecipe")
    class UpdateRecipe {

        @Test
        @DisplayName("updateRecipe_asAdmin_returnsUpdatedRecipe")
        @WithMockUser(roles = "ADMIN")
        void updateRecipe_asAdmin_returnsUpdatedRecipe() throws Exception {
            given(adminRecipeService.update(updateRecipeDto, RECIPE_ID)).willReturn(recipeDto);

            mockMvc.perform(patch(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRecipeDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RECIPE_ID));

            verify(adminRecipeService).update(updateRecipeDto, RECIPE_ID);
        }

        @Test
        @DisplayName("updateRecipe_notFound_returnsNotFound")
        @WithMockUser(roles = "ADMIN")
        void updateRecipe_notFound_returnsNotFound() throws Exception {
            given(adminRecipeService.update(updateRecipeDto, RECIPE_ID))
                    .willThrow(new EntityNotFoundException(
                            "Recipe with recipeId: " + RECIPE_ID + " not found"));

            mockMvc.perform(patch(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRecipeDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("updateRecipe_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void updateRecipe_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRecipeDto)))
                    .andExpect(status().isForbidden());

            verify(adminRecipeService, never()).update(any(), anyLong());
        }
    }

    @Nested
    @DisplayName("deleteRecipe")
    class DeleteRecipe {

        @Test
        @DisplayName("deleteRecipe_asAdmin_returnsNoContent")
        @WithMockUser(roles = "ADMIN")
        void deleteRecipe_asAdmin_returnsNoContent() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(adminRecipeService).deleteById(RECIPE_ID);
        }

        @Test
        @DisplayName("deleteRecipe_notFound_returnsNotFound")
        @WithMockUser(roles = "ADMIN")
        void deleteRecipe_notFound_returnsNotFound() throws Exception {
            org.mockito.Mockito.doThrow(new EntityNotFoundException(
                            "Recipe with recipeId: " + RECIPE_ID + " not found"))
                    .when(adminRecipeService).deleteById(RECIPE_ID);

            mockMvc.perform(delete(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deleteRecipe_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void deleteRecipe_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(adminRecipeService, never()).deleteById(anyLong());
        }
    }
}
