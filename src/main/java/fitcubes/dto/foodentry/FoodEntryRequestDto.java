package fitcubes.dto.foodentry;

import fitcubes.model.foodentry.MealType;
import fitcubes.model.foodentry.SourceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public record FoodEntryRequestDto(
        @NotNull SourceType sourceType,
        Long productId,
        Long recipeId,
        String customName,
        String label,
        BigDecimal customCalories,
        BigDecimal customProtein,
        BigDecimal customCarbs,
        BigDecimal customFat,
        @NotNull @Positive BigDecimal quantity,
        @NotNull MealType mealType,
        @NotNull @PastOrPresent Instant loggedAt
) {
}
