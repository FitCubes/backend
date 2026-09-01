package fitcubes.service.exerciseentry.impl;

import fitcubes.model.exercise.Exercise;
import fitcubes.service.exerciseentry.ExerciseCalculationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class ExerciseCalculationServiceImpl implements ExerciseCalculationService {

    private static final BigDecimal FACTOR = BigDecimal.valueOf(3.5);
    private static final BigDecimal DIVISOR = BigDecimal.valueOf(200);
    private static final int SCALE = 2;

    public BigDecimal calculateCaloriesBurned(Exercise exercise, BigDecimal weightKg,
                                              BigDecimal durationMinutes) {
        BigDecimal perMinute = exercise.getMet()
                .multiply(FACTOR)
                .multiply(weightKg)
                .divide(DIVISOR, 6, RoundingMode.HALF_UP);

        return perMinute.multiply(durationMinutes).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
