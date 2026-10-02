package fitcubes.service.exerciseentry.impl;

import fitcubes.model.exercise.Exercise;
import fitcubes.model.exercise.ExerciseUnit;
import fitcubes.service.exerciseentry.ExerciseCalculationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class ExerciseCalculationServiceImpl implements ExerciseCalculationService {

    private static final BigDecimal FACTOR = BigDecimal.valueOf(3.5);
    private static final BigDecimal DIVISOR = BigDecimal.valueOf(200);
    private static final int SCALE = 2;

    @Override
    public BigDecimal calculateCaloriesBurned(Exercise exercise, BigDecimal weightKg,
                                              BigDecimal quantity) {
        if (exercise.getUnit() == ExerciseUnit.REPS || exercise.getUnit() == ExerciseUnit.STEPS) {
            if (exercise.getCaloriesPerUnit() == null) {
                throw new IllegalStateException(
                        "Exercise " + exercise.getId() + " has unit " + exercise.getUnit()
                                + " but no caloriesPerUnit configured");
            }
            return exercise.getCaloriesPerUnit()
                    .multiply(quantity)
                    .setScale(SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal perMinute = exercise.getMet()
                .multiply(FACTOR)
                .multiply(weightKg)
                .divide(DIVISOR, 6, RoundingMode.HALF_UP);

        return perMinute.multiply(quantity).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
