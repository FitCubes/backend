package fitcubes.dto.exercise;

import fitcubes.model.exercise.ExerciseCategory;
import java.math.BigDecimal;

public record ExerciseDto(
        Long id,
        String name,
        ExerciseCategory category,
        String primaryMuscles,
        BigDecimal met
) {
}
