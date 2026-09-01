package fitcubes.service;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.RecipeIngredientDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import fitcubes.exception.AccessDeniedException;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.RecipeMapper;
import fitcubes.model.recipe.Recipe;
import fitcubes.model.recipe.RecipeCategory;
import fitcubes.model.user.User;
import fitcubes.repository.RecipeRepository;
import fitcubes.repository.UserRepository;
import fitcubes.service.impl.RecipeServiceImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(classes = RecipeServiceImpl.class)
class RecipeServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;
    private static final Long RECIPE_ID = 10L;
    private static final String RECIPE_NAME = "Oatmeal";
    private static final String CATEGORY = RecipeCategory.BREAKFAST;

    @MockitoBean
    private RecipeRepository recipeRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeServiceImpl recipeService;

    private User user;
    private Recipe recipe;
    private CreateRecipeDto createRecipeDto;
    private UpdateRecipeDto updateRecipeDto;
    private RecipeDto recipeDto;

    @BeforeEach
    void setUp() {
        user = createUser(USER_ID);
        recipe = createRecipe(RECIPE_ID, user);

        RecipeIngredientDto ingredientDto = new RecipeIngredientDto(
                1L, 1L, "Oats", 100.0, 389.0, 16.9, 66.3, 6.9, 67.6
        );

        createRecipeDto = new CreateRecipeDto(
                RECIPE_NAME, CATEGORY, "Healthy breakfast", 1, 100.0, 250.0, 150.0, 6.0, 25.0, 3.0, List.of(ingredientDto)
        );
        updateRecipeDto = new UpdateRecipeDto(
                "Protein Oatmeal", CATEGORY, "Updated description", 1, 120.0, 270.0, 180.0, 15.0, 25.0, 4.0, List.of(ingredientDto)
        );
        recipeDto = new RecipeDto(
                RECIPE_ID, USER_ID, RECIPE_NAME, CATEGORY, "Healthy breakfast", 1, 100.0, 250.0, 150.0, 6.0, 25.0, 3.0, 24.0, List.of(ingredientDto)
        );
    }

    private User createUser(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private Recipe createRecipe(Long id, User owner) {
        Recipe r = new Recipe();
        r.setId(id);
        r.setUser(owner);
        r.setName(RECIPE_NAME);
        r.setCategory(CATEGORY);
        r.setDescription("Healthy breakfast");
        r.setServings(1);
        r.setRawWeight(BigDecimal.valueOf(100.0));
        r.setCookedWeight(BigDecimal.valueOf(250.0));
        r.setCaloriesPer100g(BigDecimal.valueOf(150.0));
        r.setProteinPer100g(BigDecimal.valueOf(6.0));
        r.setCarbsPer100g(BigDecimal.valueOf(25.0));
        r.setFatsPer100g(BigDecimal.valueOf(3.0));
        return r;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("save_userExists_savesRecipeWithUser")
        void save_userExists_savesRecipeWithUser() {
            Recipe mappedRecipe = createRecipe(null, null);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(recipeMapper.toEntity(createRecipeDto)).willReturn(mappedRecipe);
            given(recipeRepository.save(mappedRecipe)).willReturn(recipe);
            given(recipeMapper.toDto(recipe)).willReturn(recipeDto);

            RecipeDto result = recipeService.save(createRecipeDto, USER_ID);

            assertThat(result).isEqualTo(recipeDto);
            assertThat(mappedRecipe.getUser()).isEqualTo(user);
            verify(recipeRepository).save(mappedRecipe);
        }

        @Test
        @DisplayName("save_userNotFound_throwsEntityNotFoundException")
        void save_userNotFound_throwsEntityNotFoundException() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recipeService.save(createRecipeDto, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(String.valueOf(USER_ID));

            verify(recipeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("deleteById_ownerMatches_deletesRecipe")
        void deleteById_ownerMatches_deletesRecipe() {
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(recipe));

            recipeService.deleteById(RECIPE_ID, USER_ID);

            verify(recipeRepository).delete(recipe);
        }

        @Test
        @DisplayName("deleteById_recipeNotFound_throwsEntityNotFoundException")
        void deleteById_recipeNotFound_throwsEntityNotFoundException() {
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recipeService.deleteById(RECIPE_ID, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(recipeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteById_notOwner_throwsAccessDeniedException")
        void deleteById_notOwner_throwsAccessDeniedException() {
            Recipe foreignRecipe = createRecipe(RECIPE_ID, createUser(OTHER_USER_ID));
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(foreignRecipe));

            assertThatThrownBy(() -> recipeService.deleteById(RECIPE_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(recipeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteById_globalRecipe_throwsAccessDeniedException")
        void deleteById_globalRecipe_throwsAccessDeniedException() {
            Recipe globalRecipe = createRecipe(RECIPE_ID, null);
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(globalRecipe));

            assertThatThrownBy(() -> recipeService.deleteById(RECIPE_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(recipeRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("update_ownerMatches_updatesRecipe")
        void update_ownerMatches_updatesRecipe() {
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(recipe));
            given(recipeRepository.save(recipe)).willReturn(recipe);
            given(recipeMapper.toDto(recipe)).willReturn(recipeDto);

            RecipeDto result = recipeService.update(updateRecipeDto, RECIPE_ID, USER_ID);

            assertThat(result).isEqualTo(recipeDto);
            verify(recipeMapper).updateRecipeDto(updateRecipeDto, recipe);
            verify(recipeRepository).save(recipe);
        }

        @Test
        @DisplayName("update_notOwner_throwsAccessDeniedException")
        void update_notOwner_throwsAccessDeniedException() {
            Recipe foreignRecipe = createRecipe(RECIPE_ID, createUser(OTHER_USER_ID));
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(foreignRecipe));

            assertThatThrownBy(() -> recipeService.update(updateRecipeDto, RECIPE_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(recipeMapper, never()).updateRecipeDto(any(), any());
            verify(recipeRepository, never()).save(any());
        }

        @Test
        @DisplayName("update_globalRecipe_throwsAccessDeniedException")
        void update_globalRecipe_throwsAccessDeniedException() {
            Recipe globalRecipe = createRecipe(RECIPE_ID, null);
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(globalRecipe));

            assertThatThrownBy(() -> recipeService.update(updateRecipeDto, RECIPE_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(recipeMapper, never()).updateRecipeDto(any(), any());
            verify(recipeRepository, never()).save(any());
        }

        @Test
        @DisplayName("update_recipeNotFound_throwsEntityNotFoundException")
        void update_recipeNotFound_throwsEntityNotFoundException() {
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recipeService.update(updateRecipeDto, RECIPE_ID, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllRecipes")
    class GetAllRecipes {

        @Test
        @DisplayName("getAllRecipes_returnsMappedPage")
        void getAllRecipes_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Recipe> recipePage = new PageImpl<>(List.of(recipe));
            given(recipeRepository.findAllGlobalOrByUserIdAndCategory(USER_ID, CATEGORY, pageable))
                    .willReturn(recipePage);
            given(recipeMapper.toDto(recipe)).willReturn(recipeDto);

            Page<RecipeDto> result = recipeService.getAllRecipes(pageable, CATEGORY, USER_ID);

            assertThat(result.getContent()).containsExactly(recipeDto);
        }

        @Test
        @DisplayName("getAllRecipes_noRecipes_returnsEmptyPage")
        void getAllRecipes_noRecipes_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            given(recipeRepository.findAllGlobalOrByUserIdAndCategory(USER_ID, CATEGORY, pageable))
                    .willReturn(Page.empty(pageable));

            Page<RecipeDto> result = recipeService.getAllRecipes(pageable, CATEGORY, USER_ID);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRecipeById")
    class GetRecipeById {

        @Test
        @DisplayName("getRecipeById_ownerMatches_returnsDto")
        void getRecipeById_ownerMatches_returnsDto() {
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(recipe));
            given(recipeMapper.toDto(recipe)).willReturn(recipeDto);

            RecipeDto result = recipeService.getRecipeById(RECIPE_ID, USER_ID);

            assertThat(result).isEqualTo(recipeDto);
        }

        @Test
        @DisplayName("getRecipeById_globalRecipe_returnsDto")
        void getRecipeById_globalRecipe_returnsDto() {
            Recipe globalRecipe = createRecipe(RECIPE_ID, null);
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(globalRecipe));
            given(recipeMapper.toDto(globalRecipe)).willReturn(recipeDto);

            RecipeDto result = recipeService.getRecipeById(RECIPE_ID, USER_ID);

            assertThat(result).isEqualTo(recipeDto);
        }

        @Test
        @DisplayName("getRecipeById_notOwner_throwsAccessDeniedException")
        void getRecipeById_notOwner_throwsAccessDeniedException() {
            Recipe foreignRecipe = createRecipe(RECIPE_ID, createUser(OTHER_USER_ID));
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(foreignRecipe));

            assertThatThrownBy(() -> recipeService.getRecipeById(RECIPE_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("getRecipeById_recipeNotFound_throwsEntityNotFoundException")
        void getRecipeById_recipeNotFound_throwsEntityNotFoundException() {
            given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recipeService.getRecipeById(RECIPE_ID, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
