package fitcubes.dto.diary;

import java.math.BigDecimal;

public record DiaryExerciseEntryDto(
        Long id,
        String name,
        BigDecimal caloriesBurned,
        BigDecimal durationMinutes
) {
}
