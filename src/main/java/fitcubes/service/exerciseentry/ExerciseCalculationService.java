package fitcubes.service.exerciseentry;

import fitcubes.model.exercise.Exercise;
import java.math.BigDecimal;

public interface ExerciseCalculationService {
    BigDecimal calculateCaloriesBurned(Exercise exercise, BigDecimal weightKg,
                                       BigDecimal durationMinutes);
}
