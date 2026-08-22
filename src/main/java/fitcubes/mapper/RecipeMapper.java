package fitcubes.mapper;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import fitcubes.model.recipe.Recipe;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {RecipeIngredientMapper.class})
public interface RecipeMapper {

    void updateRecipeDto(UpdateRecipeDto updateRecipeDto, @MappingTarget Recipe recipe);

    Recipe toEntity(CreateRecipeDto createRecipeDto);

    RecipeDto toDto(Recipe recipe);

    @AfterMapping
    default void linkIngredientsToRecipe(@MappingTarget Recipe recipe) {
        if (recipe.getIngredients() != null) {
            recipe.getIngredients().forEach(ingredient -> ingredient.setRecipe(recipe));
        }
    }
}
