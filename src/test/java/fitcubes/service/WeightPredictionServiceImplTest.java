package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fitcubes.dto.dashboard.PredictedWeightChangeResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.user.User;
import fitcubes.repository.ExerciseEntryRepository;
import fitcubes.repository.FoodEntryRepository;
import fitcubes.repository.UserRepository;
import fitcubes.service.dashboard.CalorieCalculationService;
import fitcubes.service.weight.impl.WeightPredictionServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeightPredictionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FoodEntryRepository foodEntryRepository;

    @Mock
    private ExerciseEntryRepository exerciseEntryRepository;

    @Mock
    private CalorieCalculationService calorieCalculationService;

    @InjectMocks
    private WeightPredictionServiceImpl service;

    private static final Long USER_ID = 42L;

    @Test
    void getPredictedWeeklyChange_consistentDeficit_predictsWeightLoss() {
        User user = new User();
        user.setId(USER_ID);

        Instant from = Instant.parse("2026-08-25T00:00:00Z");
        Instant to = Instant.parse("2026-09-01T00:00:00Z");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(calorieCalculationService.calculateTdee(user)).thenReturn(BigDecimal.valueOf(2500));
        when(foodEntryRepository.sumCaloriesByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.valueOf(14000));
        when(exerciseEntryRepository.sumCaloriesBurnedByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.ZERO);

        PredictedWeightChangeResponseDto result =
                service.getPredictedWeeklyChange(USER_ID, from, to);

        assertThat(result.averageDailyDeficit()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(result.predictedWeeklyChangeKg())
                .isEqualByComparingTo(BigDecimal.valueOf(-0.45));
    }

    @Test
    void getPredictedWeeklyChange_surplus_predictsWeightGain() {
        User user = new User();
        user.setId(USER_ID);

        Instant from = Instant.parse("2026-08-25T00:00:00Z");
        Instant to = Instant.parse("2026-09-01T00:00:00Z");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(calorieCalculationService.calculateTdee(user)).thenReturn(BigDecimal.valueOf(2000));
        when(foodEntryRepository.sumCaloriesByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.valueOf(17500));
        when(exerciseEntryRepository.sumCaloriesBurnedByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.ZERO);

        PredictedWeightChangeResponseDto result =
                service.getPredictedWeeklyChange(USER_ID, from, to);

        assertThat(result.averageDailyDeficit()).isEqualByComparingTo(BigDecimal.valueOf(-500.00));
        assertThat(result.predictedWeeklyChangeKg())
                .isEqualByComparingTo(BigDecimal.valueOf(0.45));
    }

    @Test
    void getPredictedWeeklyChange_exerciseIncreasesDeficit() {
        User user = new User();
        user.setId(USER_ID);

        Instant from = Instant.parse("2026-08-25T00:00:00Z");
        Instant to = Instant.parse("2026-09-01T00:00:00Z");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(calorieCalculationService.calculateTdee(user)).thenReturn(BigDecimal.valueOf(2000));
        when(foodEntryRepository.sumCaloriesByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.valueOf(14000));
        when(exerciseEntryRepository.sumCaloriesBurnedByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.valueOf(3500));

        PredictedWeightChangeResponseDto result =
                service.getPredictedWeeklyChange(USER_ID, from, to);

        assertThat(result.averageDailyDeficit()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(result.predictedWeeklyChangeKg())
                .isEqualByComparingTo(BigDecimal.valueOf(-0.45));
    }

    @Test
    void getPredictedWeeklyChange_differentRangeLength_extrapolatesToWeek() {
        User user = new User();
        user.setId(USER_ID);

        Instant from = Instant.parse("2026-08-25T00:00:00Z");
        Instant to = Instant.parse("2026-08-27T00:00:00Z");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(calorieCalculationService.calculateTdee(user)).thenReturn(BigDecimal.valueOf(2500));
        when(foodEntryRepository.sumCaloriesByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.valueOf(4000));
        when(exerciseEntryRepository.sumCaloriesBurnedByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.ZERO);

        PredictedWeightChangeResponseDto result =
                service.getPredictedWeeklyChange(USER_ID, from, to);

        assertThat(result.averageDailyDeficit()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(result.predictedWeeklyChangeKg())
                .isEqualByComparingTo(BigDecimal.valueOf(-0.45));
    }

    @Test
    void getPredictedWeeklyChange_noDeficit_predictsNoChange() {
        User user = new User();
        user.setId(USER_ID);

        Instant from = Instant.parse("2026-08-25T00:00:00Z");
        Instant to = Instant.parse("2026-09-01T00:00:00Z");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(calorieCalculationService.calculateTdee(user)).thenReturn(BigDecimal.valueOf(2000));
        when(foodEntryRepository.sumCaloriesByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.valueOf(14000));
        when(exerciseEntryRepository.sumCaloriesBurnedByUserIdAndLoggedAtBetween(USER_ID, from, to))
                .thenReturn(BigDecimal.ZERO);

        PredictedWeightChangeResponseDto result =
                service.getPredictedWeeklyChange(USER_ID, from, to);

        assertThat(result.averageDailyDeficit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.predictedWeeklyChangeKg()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getPredictedWeeklyChange_invalidRange_throwsIllegalArgument() {
        User user = new User();
        user.setId(USER_ID);
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-25T00:00:00Z");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.getPredictedWeeklyChange(USER_ID, from, to))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getPredictedWeeklyChange_userNotFound_throwsEntityNotFound() {
        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPredictedWeeklyChange(USER_ID, from, to))
                .isInstanceOf(EntityNotFoundException.class);
    }
}