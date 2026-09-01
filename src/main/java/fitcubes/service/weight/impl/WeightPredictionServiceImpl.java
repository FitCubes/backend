package fitcubes.service.weight.impl;

import fitcubes.dto.dashboard.PredictedWeightChangeResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.user.User;
import fitcubes.repository.ExerciseEntryRepository;
import fitcubes.repository.FoodEntryRepository;
import fitcubes.repository.UserRepository;
import fitcubes.service.dashboard.CalorieCalculationService;
import fitcubes.service.weight.WeightPredictionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeightPredictionServiceImpl implements WeightPredictionService {

    private static final BigDecimal CALORIES_PER_KG = BigDecimal.valueOf(7700);
    private static final BigDecimal SECONDS_PER_DAY = BigDecimal.valueOf(86400);
    private static final BigDecimal DAYS_PER_WEEK = BigDecimal.valueOf(7);
    private static final int SCALE = 2;
    private static final int INTERMEDIATE_SCALE = 6;

    private final UserRepository userRepository;
    private final FoodEntryRepository foodEntryRepository;
    private final ExerciseEntryRepository exerciseEntryRepository;
    private final CalorieCalculationService calorieCalculationService;

    @Override
    public PredictedWeightChangeResponseDto getPredictedWeeklyChange(Long userId, Instant from,
                                                                     Instant to) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        BigDecimal days = daysBetween(from, to);
        if (days.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Date range must span at least one day");
        }

        BigDecimal tdee = calorieCalculationService.calculateTdee(user);
        BigDecimal consumed = foodEntryRepository
                .sumCaloriesByUserIdAndLoggedAtBetween(userId, from, to);
        BigDecimal burned = exerciseEntryRepository
                .sumCaloriesBurnedByUserIdAndLoggedAtBetween(userId, from, to);

        BigDecimal totalMaintenanceCalories = tdee.multiply(days);
        BigDecimal totalDeficit = totalMaintenanceCalories.subtract(consumed).add(burned);

        BigDecimal averageDailyDeficit = totalDeficit
                .divide(days, INTERMEDIATE_SCALE, RoundingMode.HALF_UP)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal weeklyDeficit = totalDeficit
                .divide(days, INTERMEDIATE_SCALE, RoundingMode.HALF_UP)
                .multiply(DAYS_PER_WEEK);

        BigDecimal predictedWeeklyChangeKg = weeklyDeficit
                .divide(CALORIES_PER_KG, INTERMEDIATE_SCALE, RoundingMode.HALF_UP)
                .negate()
                .setScale(SCALE, RoundingMode.HALF_UP);

        return new PredictedWeightChangeResponseDto(averageDailyDeficit, predictedWeeklyChangeKg);
    }

    private BigDecimal daysBetween(Instant from, Instant to) {
        long seconds = Duration.between(from, to).getSeconds();
        return BigDecimal.valueOf(seconds)
                .divide(SECONDS_PER_DAY, INTERMEDIATE_SCALE, RoundingMode.HALF_UP);
    }
}
