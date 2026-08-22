package fitcubes.dto.recipe;

import java.util.List;

public record RecipeDto(
        Long id,
        Long userId,
        String name,
        String category,
        String description,
        Integer servings,
        Double rawWeight,
        Double cookedWeight,
        Double caloriesPer100g,
        Double proteinPer100g,
        Double carbsPer100g,
        Double fatsPer100g,
        Double proteinCaloriesPer100g,
        List<RecipeIngredientDto> ingredients
) {
    public RecipeDto {
        if (proteinPer100g != null && proteinCaloriesPer100g == null) {
            proteinCaloriesPer100g = proteinPer100g * 4.0;
        }
    }
}
