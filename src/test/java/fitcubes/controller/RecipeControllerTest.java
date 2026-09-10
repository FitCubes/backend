package fitcubes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fitcubes.config.TestSecurityConfig;
import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.RecipeIngredientDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import fitcubes.exception.AccessDeniedException;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.recipe.RecipeCategory;
import fitcubes.model.user.Role;
import fitcubes.model.user.RoleName;
import fitcubes.model.user.User;
import fitcubes.security.JwtUtil;
import fitcubes.security.TokenBlacklistService;
import fitcubes.service.recipe.RecipeService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@Import(TestSecurityConfig.class)
public class RecipeControllerTest {

    private static final String BASE_URL = "/api/v1/recipes";
    private static final Long RECIPE_ID = 10L;
    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private CreateRecipeDto createRecipeDto;
    private UpdateRecipeDto updateRecipeDto;
    private RecipeDto recipeDto;
    private User user;

    @BeforeEach
    void setUp() {
        RecipeIngredientDto ingredientDto = new RecipeIngredientDto(
                1L,
                1L,
                "Oats",
                100.0,
                389.0,
                16.9,
                66.3,
                6.9,
                67.6
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
                USER_ID,
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
                24.0,
                List.of(ingredientDto)
        );

        user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");

        Role userRole = new Role();
        userRole.setName(RoleName.USER);

        user.setRoles(Set.of(userRole));
    }

    private UsernamePasswordAuthenticationToken userAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
    }

    @Nested
    @DisplayName("createRecipe")
    class CreateRecipe {

        @Test
        @DisplayName("createRecipe_asUser_returnsCreatedRecipe")
        void createRecipe_asUser_returnsCreatedRecipe() throws Exception {
            given(recipeService.save(createRecipeDto, USER_ID))
                    .willReturn(recipeDto);

            mockMvc.perform(post(BASE_URL)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRecipeDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(RECIPE_ID))
                    .andExpect(jsonPath("$.name").value("Oatmeal"))
                    .andExpect(jsonPath("$.proteinCaloriesPer100g").value(24.0));

            verify(recipeService).save(createRecipeDto, USER_ID);
        }

        @Test
        @DisplayName("createRecipe_blankName_returnsBadRequest")
        void createRecipe_blankName_returnsBadRequest() throws Exception {
            CreateRecipeDto invalidDto = new CreateRecipeDto(
                    " ",
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
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(recipeService, never()).save(any(), anyLong());
        }

        @Test
        @DisplayName("createRecipe_emptyIngredients_returnsBadRequest")
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
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(recipeService, never()).save(any(), anyLong());
        }
    }

    @Nested
    @DisplayName("getAllRecipes")
    class GetAllRecipes {

        @Test
        @DisplayName("getAllRecipes_asUser_returnsPage")
        void getAllRecipes_asUser_returnsPage() throws Exception {
            Page<RecipeDto> page = new PageImpl<>(List.of(recipeDto));

            given(recipeService.getAllRecipes(
                    any(Pageable.class),
                    eq(RecipeCategory.BREAKFAST),
                    eq(USER_ID)
            )).willReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .param("category", "BREAKFAST")
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(RECIPE_ID))
                    .andExpect(jsonPath("$.content[0].proteinCaloriesPer100g").value(24.0));

            verify(recipeService).getAllRecipes(
                    any(Pageable.class),
                    eq(RecipeCategory.BREAKFAST),
                    eq(USER_ID)
            );
        }
    }

    @Nested
    @DisplayName("getRecipeById")
    class GetRecipeById {

        @Test
        @DisplayName("getRecipeById_asUser_returnsRecipe")
        void getRecipeById_asUser_returnsRecipe() throws Exception {
            given(recipeService.getRecipeById(RECIPE_ID, USER_ID))
                    .willReturn(recipeDto);

            mockMvc.perform(get(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RECIPE_ID))
                    .andExpect(jsonPath("$.proteinCaloriesPer100g").value(24.0));

            verify(recipeService).getRecipeById(RECIPE_ID, USER_ID);
        }

        @Test
        @DisplayName("getRecipeById_notFound_returnsNotFound")
        void getRecipeById_notFound_returnsNotFound() throws Exception {
            given(recipeService.getRecipeById(RECIPE_ID, USER_ID))
                    .willThrow(new EntityNotFoundException(
                            "Recipe with recipeId: " + RECIPE_ID + " not found"
                    ));

            mockMvc.perform(get(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("getRecipeById_forbidden_returnsForbidden")
        void getRecipeById_forbidden_returnsForbidden() throws Exception {
            given(recipeService.getRecipeById(RECIPE_ID, USER_ID))
                    .willThrow(new AccessDeniedException("Access denied"));

            mockMvc.perform(get(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("updateRecipe")
    class UpdateRecipe {

        @Test
        @DisplayName("updateRecipe_asUser_returnsUpdatedRecipe")
        void updateRecipe_asUser_returnsUpdatedRecipe() throws Exception {
            given(recipeService.update(
                    updateRecipeDto,
                    RECIPE_ID,
                    USER_ID
            )).willReturn(recipeDto);

            mockMvc.perform(patch(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRecipeDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RECIPE_ID));

            verify(recipeService).update(
                    updateRecipeDto,
                    RECIPE_ID,
                    USER_ID
            );
        }

        @Test
        @DisplayName("updateRecipe_notFound_returnsNotFound")
        void updateRecipe_notFound_returnsNotFound() throws Exception {
            given(recipeService.update(
                    updateRecipeDto,
                    RECIPE_ID,
                    USER_ID
            )).willThrow(new EntityNotFoundException(
                    "Recipe with recipeId: " + RECIPE_ID + " not found"
            ));

            mockMvc.perform(patch(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRecipeDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("updateRecipe_forbidden_returnsForbidden")
        void updateRecipe_forbidden_returnsForbidden() throws Exception {
            given(recipeService.update(
                    updateRecipeDto,
                    RECIPE_ID,
                    USER_ID
            )).willThrow(new AccessDeniedException("Access denied"));

            mockMvc.perform(patch(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRecipeDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("deleteRecipe")
    class DeleteRecipe {

        @Test
        @DisplayName("deleteRecipe_asUser_returnsNoContent")
        void deleteRecipe_asUser_returnsNoContent() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isNoContent());

            verify(recipeService).deleteById(RECIPE_ID, USER_ID);
        }

        @Test
        @DisplayName("deleteRecipe_notFound_returnsNotFound")
        void deleteRecipe_notFound_returnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException(
                    "Recipe with recipeId: " + RECIPE_ID + " not found"
            )).when(recipeService).deleteById(RECIPE_ID, USER_ID);

            mockMvc.perform(delete(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deleteRecipe_forbidden_returnsForbidden")
        void deleteRecipe_forbidden_returnsForbidden() throws Exception {
            doThrow(new AccessDeniedException("Access denied"))
                    .when(recipeService)
                    .deleteById(RECIPE_ID, USER_ID);

            mockMvc.perform(delete(BASE_URL + "/{recipeId}", RECIPE_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isForbidden());
        }
    }
}
