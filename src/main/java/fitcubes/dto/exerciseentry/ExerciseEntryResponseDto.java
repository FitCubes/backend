package fitcubes.dto.exerciseentry;

import java.math.BigDecimal;
import java.time.Instant;

public record ExerciseEntryResponseDto(
        Long id,
        Long exerciseId,
        String nameSnapshot,
        BigDecimal durationMinutes,
        BigDecimal caloriesBurned,
        Instant loggedAt
) {
}
