package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fitcubes.model.exercise.Exercise;
import fitcubes.service.exerciseentry.impl.ExerciseCalculationServiceImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ExerciseCalculationServiceImplTest {

    private final ExerciseCalculationServiceImpl service = new ExerciseCalculationServiceImpl();

    @Test
    void calculateCaloriesBurned_computesCorrectValue() {
        Exercise running = mock(Exercise.class);
        when(running.getMet()).thenReturn(BigDecimal.valueOf(8));

        // (8 * 3.5 * 70) / 200 = 9.8 kcal/min; * 30 min = 294.00
        BigDecimal result = service.calculateCaloriesBurned(
                running, BigDecimal.valueOf(70), BigDecimal.valueOf(30));

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(294.00));
    }

    @Test
    void calculateCaloriesBurned_differentWeight_producesDifferentResult() {
        Exercise running = mock(Exercise.class);
        when(running.getMet()).thenReturn(BigDecimal.valueOf(8));

        BigDecimal resultLighter = service.calculateCaloriesBurned(
                running, BigDecimal.valueOf(50), BigDecimal.valueOf(30));
        BigDecimal resultHeavier = service.calculateCaloriesBurned(
                running, BigDecimal.valueOf(90), BigDecimal.valueOf(30));

        assertThat(resultLighter).isLessThan(resultHeavier);
    }

    @Test
    void calculateCaloriesBurned_fractionalDuration_roundsToTwoDecimals() {
        Exercise yoga = mock(Exercise.class);
        when(yoga.getMet()).thenReturn(BigDecimal.valueOf(2.5));

        BigDecimal result = service.calculateCaloriesBurned(
                yoga, BigDecimal.valueOf(65), BigDecimal.valueOf(15.5));

        // (2.5 * 3.5 * 65) / 200 = 2.84375 kcal/min; * 15.5 = 44.078125 -> 44.08
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(44.08));
    }

    @Test
    void calculateCaloriesBurned_zeroDuration_returnsZero() {
        Exercise exercise = mock(Exercise.class);
        when(exercise.getMet()).thenReturn(BigDecimal.valueOf(6));

        BigDecimal result = service.calculateCaloriesBurned(
                exercise, BigDecimal.valueOf(80), BigDecimal.ZERO);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
