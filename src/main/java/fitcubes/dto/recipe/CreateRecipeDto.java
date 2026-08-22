package fitcubes.dto.recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRecipeDto(
        @NotBlank(message = "Recipe name cannot be blank")
        @Size(max = 100, message = "Recipe name cannot exceed 100 characters")
        String name,

        @NotNull(message = "Category is required")
        String category,

        String description,

        @NotNull(message = "Servings count is required")
        @Positive(message = "Servings must be at least 1")
        Integer servings,

        @NotNull(message = "Raw weight is required")
        @Positive(message = "Raw weight must be greater than zero")
        Double rawWeight,

        @NotNull(message = "Cooked weight is required")
        @Positive(message = "Cooked weight must be greater than zero")
        Double cookedWeight,

        @NotNull(message = "Calories per 100g is required")
        @PositiveOrZero(message = "Calories cannot be negative")
        Double caloriesPer100g,

        @NotNull(message = "Protein per 100g is required")
        @PositiveOrZero(message = "Protein cannot be negative")
        Double proteinPer100g,

        @NotNull(message = "Carbs per 100g is required")
        @PositiveOrZero(message = "Carbs cannot be negative")
        Double carbsPer100g,

        @NotNull(message = "Fats per 100g is required")
        @PositiveOrZero(message = "Fats cannot be negative")
        Double fatsPer100g,

        @NotEmpty(message = "Recipe must contain at least one ingredient")
        @Valid
        List<RecipeIngredientDto> ingredients
) {
}
