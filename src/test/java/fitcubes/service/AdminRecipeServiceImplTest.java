package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.RecipeIngredientDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.RecipeMapper;
import fitcubes.model.recipe.Recipe;
import fitcubes.model.recipe.RecipeCategory;
import fitcubes.model.user.User;
import fitcubes.repository.RecipeRepository;
import fitcubes.service.impl.AdminRecipeServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = AdminRecipeServiceImpl.class)
public class AdminRecipeServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;
    private static final Long RECIPE_ID = 10L;
    private static final String RECIPE_NAME = "Oatmeal";
    private static final String CATEGORY = RecipeCategory.BREAKFAST;

    @MockitoBean
    private RecipeRepository recipeRepository;

    @MockitoBean
    private RecipeMapper recipeMapper;

    @Autowired
    private AdminRecipeServiceImpl adminRecipeService;

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
        r.setRawWeight(100.0);
        r.setCookedWeight(250.0);
        r.setCaloriesPer100g(150.0);
        r.setProteinPer100g(6.0);
        r.setCarbsPer100g(25.0);
        r.setFatsPer100g(3.0);
        return r;
    }

    @Test
    @DisplayName("save_savesRecipeWithoutOwnerCheck")
    void save_savesRecipeWithoutOwnerCheck() {
        Recipe mappedRecipe = createRecipe(null, null);
        given(recipeMapper.toEntity(createRecipeDto)).willReturn(mappedRecipe);
        given(recipeRepository.save(mappedRecipe)).willReturn(recipe);
        given(recipeMapper.toDto(recipe)).willReturn(recipeDto);

        RecipeDto result = adminRecipeService.save(createRecipeDto);

        assertThat(result).isEqualTo(recipeDto);
        verify(recipeRepository).save(mappedRecipe);
    }

    @Test
    @DisplayName("getRecipeById_returnsDtoRegardlessOfOwner")
    void getRecipeById_returnsDtoRegardlessOfOwner() {
        Recipe foreignRecipe = createRecipe(RECIPE_ID, createUser(OTHER_USER_ID));
        given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(foreignRecipe));
        given(recipeMapper.toDto(foreignRecipe)).willReturn(recipeDto);

        RecipeDto result = adminRecipeService.getRecipeById(RECIPE_ID);

        assertThat(result).isEqualTo(recipeDto);
    }

    @Test
    @DisplayName("getRecipeById_notFound_throwsEntityNotFoundException")
    void getRecipeById_notFound_throwsEntityNotFoundException() {
        given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminRecipeService.getRecipeById(RECIPE_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Not found");
    }

    @Test
    @DisplayName("deleteById_deletesRecipeRegardlessOfOwner")
    void deleteById_deletesRecipeRegardlessOfOwner() {
        Recipe foreignRecipe = createRecipe(RECIPE_ID, createUser(OTHER_USER_ID));
        given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(foreignRecipe));

        adminRecipeService.deleteById(RECIPE_ID);

        verify(recipeRepository).delete(foreignRecipe);
    }

    @Test
    @DisplayName("deleteById_notFound_throwsEntityNotFoundException")
    void deleteById_notFound_throwsEntityNotFoundException() {
        given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminRecipeService.deleteById(RECIPE_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Not found");

        verify(recipeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("update_updatesRecipeRegardlessOfOwner")
    void update_updatesRecipeRegardlessOfOwner() {
        Recipe foreignRecipe = createRecipe(RECIPE_ID, createUser(OTHER_USER_ID));
        given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.of(foreignRecipe));
        given(recipeRepository.save(foreignRecipe)).willReturn(foreignRecipe);
        given(recipeMapper.toDto(foreignRecipe)).willReturn(recipeDto);

        RecipeDto result = adminRecipeService.update(updateRecipeDto, RECIPE_ID);

        assertThat(result).isEqualTo(recipeDto);
        verify(recipeMapper).updateRecipeDto(updateRecipeDto, foreignRecipe);
        verify(recipeRepository).save(foreignRecipe);
    }

    @Test
    @DisplayName("update_notFound_throwsEntityNotFoundException")
    void update_notFound_throwsEntityNotFoundException() {
        given(recipeRepository.findById(RECIPE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminRecipeService.update(updateRecipeDto, RECIPE_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Not found");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllRecipes_returnsAllRecipes")
    void getAllRecipes_returnsAllRecipes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recipe> recipePage = new PageImpl<>(List.of(recipe));
        given(recipeRepository.findAll(pageable)).willReturn(recipePage);
        given(recipeMapper.toDto(recipe)).willReturn(recipeDto);

        Page<RecipeDto> result = adminRecipeService.getAllRecipes(pageable);

        assertThat(result.getContent()).containsExactly(recipeDto);
    }
}
