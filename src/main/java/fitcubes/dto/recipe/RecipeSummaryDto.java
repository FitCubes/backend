package fitcubes.dto.recipe;

public record RecipeSummaryDto(
        Long id,
        Long userId,
        String name,
        String category,
        Integer servings,
        Double rawWeight,
        Double cookedWeight,
        Double caloriesPer100g,
        Double proteinPer100g,
        Double carbsPer100g,
        Double fatsPer100g,
        Integer ingredientsCount
) {
}
