package fitcubes.dto.exercise;

import fitcubes.model.exercise.ExerciseCategory;
import fitcubes.model.exercise.ExerciseUnit;
import java.math.BigDecimal;

public record ExerciseDto(
        Long id,
        String name,
        ExerciseCategory category,
        String primaryMuscles,
        BigDecimal met,
        ExerciseUnit unit,
        BigDecimal caloriesPerUnit
) {
}
