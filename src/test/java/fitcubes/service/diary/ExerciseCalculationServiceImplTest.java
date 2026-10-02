package fitcubes.service.diary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fitcubes.model.exercise.Exercise;
import fitcubes.model.exercise.ExerciseUnit;
import fitcubes.service.exerciseentry.impl.ExerciseCalculationServiceImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ExerciseCalculationServiceImplTest {

    private final ExerciseCalculationServiceImpl service = new ExerciseCalculationServiceImpl();

    @Test
    void calculateCaloriesBurned_minutesUnit_computesCorrectValue() {
        Exercise running = mock(Exercise.class);
        when(running.getUnit()).thenReturn(ExerciseUnit.MINUTES);
        when(running.getMet()).thenReturn(BigDecimal.valueOf(8));

        // (8 * 3.5 * 70) / 200 = 9.8 kcal/min; * 30 min = 294.00
        BigDecimal result = service.calculateCaloriesBurned(
                running, BigDecimal.valueOf(70), BigDecimal.valueOf(30));

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(294.00));
    }

    @Test
    void calculateCaloriesBurned_minutesUnit_differentWeight_producesDifferentResult() {
        Exercise running = mock(Exercise.class);
        when(running.getUnit()).thenReturn(ExerciseUnit.MINUTES);
        when(running.getMet()).thenReturn(BigDecimal.valueOf(8));

        BigDecimal resultLighter = service.calculateCaloriesBurned(
                running, BigDecimal.valueOf(50), BigDecimal.valueOf(30));
        BigDecimal resultHeavier = service.calculateCaloriesBurned(
                running, BigDecimal.valueOf(90), BigDecimal.valueOf(30));

        assertThat(resultLighter).isLessThan(resultHeavier);
    }

    @Test
    void calculateCaloriesBurned_minutesUnit_fractionalDuration_roundsToTwoDecimals() {
        Exercise yoga = mock(Exercise.class);
        when(yoga.getUnit()).thenReturn(ExerciseUnit.MINUTES);
        when(yoga.getMet()).thenReturn(BigDecimal.valueOf(2.5));

        BigDecimal result = service.calculateCaloriesBurned(
                yoga, BigDecimal.valueOf(65), BigDecimal.valueOf(15.5));

        // (2.5 * 3.5 * 65) / 200 = 2.84375 kcal/min; * 15.5 = 44.078125 -> 44.08
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(44.08));
    }

    @Test
    void calculateCaloriesBurned_minutesUnit_zeroDuration_returnsZero() {
        Exercise exercise = mock(Exercise.class);
        when(exercise.getUnit()).thenReturn(ExerciseUnit.MINUTES);
        when(exercise.getMet()).thenReturn(BigDecimal.valueOf(6));

        BigDecimal result = service.calculateCaloriesBurned(
                exercise, BigDecimal.valueOf(80), BigDecimal.ZERO);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateCaloriesBurned_repsUnit_multipliesCaloriesPerUnitByQuantity() {
        Exercise pushUps = mock(Exercise.class);
        when(pushUps.getUnit()).thenReturn(ExerciseUnit.REPS);
        when(pushUps.getCaloriesPerUnit()).thenReturn(BigDecimal.valueOf(0.4100));

        // 20 reps * 0.41 kcal/rep = 8.20
        BigDecimal result = service.calculateCaloriesBurned(
                pushUps, BigDecimal.valueOf(70), BigDecimal.valueOf(20));

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(8.20));
    }

    @Test
    void calculateCaloriesBurned_repsUnit_ignoresWeight() {
        Exercise pushUps = mock(Exercise.class);
        when(pushUps.getUnit()).thenReturn(ExerciseUnit.REPS);
        when(pushUps.getCaloriesPerUnit()).thenReturn(BigDecimal.valueOf(0.4100));

        BigDecimal resultLightUser = service.calculateCaloriesBurned(
                pushUps, BigDecimal.valueOf(50), BigDecimal.valueOf(20));
        BigDecimal resultHeavyUser = service.calculateCaloriesBurned(
                pushUps, BigDecimal.valueOf(120), BigDecimal.valueOf(20));

        assertThat(resultLightUser).isEqualByComparingTo(resultHeavyUser);
    }

    @Test
    void calculateCaloriesBurned_stepsUnit_multipliesCaloriesPerUnitByQuantity() {
        Exercise walking = mock(Exercise.class);
        when(walking.getUnit()).thenReturn(ExerciseUnit.STEPS);
        when(walking.getCaloriesPerUnit()).thenReturn(BigDecimal.valueOf(0.03));

        // 10000 steps * 0.03 kcal/step = 300.00
        BigDecimal result = service.calculateCaloriesBurned(
                walking, BigDecimal.valueOf(70), BigDecimal.valueOf(10000));

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300.00));
    }

    @Test
    void calculateCaloriesBurned_repsUnitMissingCaloriesPerUnit_throwsIllegalStateException() {
        Exercise broken = mock(Exercise.class);
        when(broken.getId()).thenReturn(99L);
        when(broken.getUnit()).thenReturn(ExerciseUnit.REPS);
        when(broken.getCaloriesPerUnit()).thenReturn(null);

        assertThatThrownBy(() -> service.calculateCaloriesBurned(
                broken, BigDecimal.valueOf(70), BigDecimal.valueOf(10)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("99");
    }
}
