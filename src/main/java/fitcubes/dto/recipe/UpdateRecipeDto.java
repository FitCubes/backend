package fitcubes.dto.recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateRecipeDto(
        @Size(min = 1, max = 100, message = "Recipe name must be between 1 and 100 characters")
        String name,

        String category,

        String description,

        @Positive(message = "Servings must be at least 1")
        Integer servings,

        @Positive(message = "Raw weightlog must be greater than zero")
        Double rawWeight,

        @Positive(message = "Cooked weightlog must be greater than zero")
        Double cookedWeight,

        @PositiveOrZero(message = "Calories cannot be negative")
        Double caloriesPer100g,

        @PositiveOrZero(message = "Protein cannot be negative")
        Double proteinPer100g,

        @PositiveOrZero(message = "Carbs cannot be negative")
        Double carbsPer100g,

        @PositiveOrZero(message = "Fats cannot be negative")
        Double fatsPer100g,

        @Valid
        List<RecipeIngredientDto> ingredients
) {
}
