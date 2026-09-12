package fitcubes.service.recipe;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.RecipeSummaryDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminRecipeService {

    RecipeDto save(CreateRecipeDto createRecipeDto);

    RecipeDto getRecipeById(Long recipeId);

    void deleteById(Long recipeId);

    RecipeDto update(UpdateRecipeDto updateRecipeDto, Long recipeId);

    Page<RecipeSummaryDto> getAllRecipes(Pageable pageable);
}
