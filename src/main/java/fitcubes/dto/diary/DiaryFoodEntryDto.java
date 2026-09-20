package fitcubes.dto.diary;

import java.math.BigDecimal;

public record DiaryFoodEntryDto(
        Long id,
        String name,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fats,
        BigDecimal weightGrams,
        String mealType
) {
}
