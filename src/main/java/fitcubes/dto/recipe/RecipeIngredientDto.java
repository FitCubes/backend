package fitcubes.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RecipeIngredientDto(
        Long id,

        @NotNull(message = "Food item ID is required")
        Long foodItemId,

        @NotBlank(message = "Ingredient name cannot be blank")
        String name,

        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be greater than zero")
        Double weight,

        @NotNull(message = "Calories value is required")
        @PositiveOrZero(message = "Calories cannot be negative")
        Double caloriesPer100g,

        @NotNull(message = "Protein value is required")
        @PositiveOrZero(message = "Protein cannot be negative")
        Double proteinPer100g,

        @NotNull(message = "Carbs value is required")
        @PositiveOrZero(message = "Carbs cannot be negative")
        Double carbsPer100g,

        @NotNull(message = "Fats value is required")
        @PositiveOrZero(message = "Fats cannot be negative")
        Double fatsPer100g,

        Double proteinCaloriesPer100g
) {
    public RecipeIngredientDto {
        if (proteinPer100g != null && proteinCaloriesPer100g == null) {
            proteinCaloriesPer100g = proteinPer100g * 4.0;
        }
    }
}
