package fitcubes.service.dashboard.impl;

import fitcubes.dto.dashboard.DailySummaryResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.user.User;
import fitcubes.repository.ExerciseEntryRepository;
import fitcubes.repository.FoodEntryRepository;
import fitcubes.repository.UserRepository;
import fitcubes.service.dashboard.CalorieCalculationService;
import fitcubes.service.dashboard.DailySummaryService;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailySummaryServiceImpl implements DailySummaryService {

    private final UserRepository userRepository;
    private final FoodEntryRepository foodEntryRepository;
    private final ExerciseEntryRepository exerciseEntryRepository;
    private final CalorieCalculationService calorieCalculationService;

    @Override
    public DailySummaryResponseDto getDailySummary(Long userId, Instant from, Instant to) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        BigDecimal target = calorieCalculationService.calculateTargetCalories(user);
        BigDecimal consumed = foodEntryRepository
                .sumCaloriesByUserIdAndLoggedAtBetween(userId, from, to);
        BigDecimal burned = exerciseEntryRepository
                .sumCaloriesBurnedByUserIdAndLoggedAtBetween(userId, from, to);

        BigDecimal remaining = target.subtract(consumed).add(burned);

        return new DailySummaryResponseDto(target, consumed, burned, remaining);
    }
}
