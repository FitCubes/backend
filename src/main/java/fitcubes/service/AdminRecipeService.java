package fitcubes.service;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminRecipeService {

    RecipeDto save(CreateRecipeDto createRecipeDto);

    RecipeDto getRecipeById(Long recipeId);

    void deleteById(Long recipeId);

    RecipeDto update(UpdateRecipeDto updateRecipeDto, Long recipeId);

    Page<RecipeDto> getAllRecipes(Pageable pageable);
}
