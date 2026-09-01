package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fitcubes.dto.dashboard.DailySummaryResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.user.User;
import fitcubes.repository.ExerciseEntryRepository;
import fitcubes.repository.FoodEntryRepository;
import fitcubes.repository.UserRepository;
import fitcubes.service.dashboard.CalorieCalculationService;
import fitcubes.service.dashboard.impl.DailySummaryServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailySummaryServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FoodEntryRepository foodEntryRepository;

    @Mock
    private ExerciseEntryRepository exerciseEntryRepository;

    @Mock
    private CalorieCalculationService calorieCalculationService;

    @InjectMocks
    private DailySummaryServiceImpl service;

    private static final Long USER_ID = 42L;
    private static final Instant FROM = Instant.parse("2026-09-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-09-01T23:59:59Z");

    @Test
    void getDailySummary_computesRemainingCorrectly() {
        User user = new User();
        user.setId(USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(calorieCalculationService.calculateTargetCalories(user))
                .thenReturn(BigDecimal.valueOf(2200));
        when(foodEntryRepository.sumCaloriesByUserIdAndLoggedAtBetween(USER_ID, FROM, TO))
                .thenReturn(BigDecimal.valueOf(1500));
        when(exerciseEntryRepository.sumCaloriesBurnedByUserIdAndLoggedAtBetween(USER_ID, FROM, TO))
                .thenReturn(BigDecimal.valueOf(300));

        DailySummaryResponseDto result = service.getDailySummary(USER_ID, FROM, TO);

        assertThat(result.targetCalories()).isEqualByComparingTo(BigDecimal.valueOf(2200));
        assertThat(result.consumed()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(result.burned()).isEqualByComparingTo(BigDecimal.valueOf(300));
        // 2200 - 1500 + 300 = 1000
        assertThat(result.remaining()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void getDailySummary_noEntries_remainingEqualsTarget() {
        User user = new User();
        user.setId(USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(calorieCalculationService.calculateTargetCalories(user))
                .thenReturn(BigDecimal.valueOf(2000));
        when(foodEntryRepository.sumCaloriesByUserIdAndLoggedAtBetween(USER_ID, FROM, TO))
                .thenReturn(BigDecimal.ZERO);
        when(exerciseEntryRepository.sumCaloriesBurnedByUserIdAndLoggedAtBetween(USER_ID, FROM, TO))
                .thenReturn(BigDecimal.ZERO);

        DailySummaryResponseDto result = service.getDailySummary(USER_ID, FROM, TO);

        assertThat(result.remaining()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    void getDailySummary_exceededTarget_remainingIsNegative() {
        User user = new User();
        user.setId(USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(calorieCalculationService.calculateTargetCalories(user))
                .thenReturn(BigDecimal.valueOf(2000));
        when(foodEntryRepository.sumCaloriesByUserIdAndLoggedAtBetween(USER_ID, FROM, TO))
                .thenReturn(BigDecimal.valueOf(2800));
        when(exerciseEntryRepository.sumCaloriesBurnedByUserIdAndLoggedAtBetween(USER_ID, FROM, TO))
                .thenReturn(BigDecimal.ZERO);

        DailySummaryResponseDto result = service.getDailySummary(USER_ID, FROM, TO);

        // 2000 - 2800 + 0 = -800
        assertThat(result.remaining()).isEqualByComparingTo(BigDecimal.valueOf(-800));
    }

    @Test
    void getDailySummary_userNotFound_throwsEntityNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDailySummary(USER_ID, FROM, TO))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
