package fitcubes.service;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecipeService {

    RecipeDto save(CreateRecipeDto createRecipeDto, Long userId);

    RecipeDto getRecipeById(Long recipeId, Long userId);

    void deleteById(Long recipeId, Long userId);

    RecipeDto update(UpdateRecipeDto updateRecipeDto, Long recipeId, Long userId);

    Page<RecipeDto> getAllRecipes(Pageable pageable, String category, Long userId);
}
