package fitcubes.mapper;

import fitcubes.dto.recipe.RecipeIngredientDto;
import fitcubes.model.recipe.RecipeIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecipeIngredientMapper {

    RecipeIngredientDto toDto(RecipeIngredient entity);

    RecipeIngredient toEntity(RecipeIngredientDto dto);

}
