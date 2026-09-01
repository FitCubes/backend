package fitcubes.dto.foodentry;

import fitcubes.model.foodentry.MealType;
import fitcubes.model.foodentry.SourceType;
import java.math.BigDecimal;
import java.time.Instant;

public record FoodEntryResponseDto(
        Long id,
        SourceType sourceType,
        Long productId,
        Long recipeId,
        String nameSnapshot,
        BigDecimal quantity,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        MealType mealType,
        Instant loggedAt
) {
}
