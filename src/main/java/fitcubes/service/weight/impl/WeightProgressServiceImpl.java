package fitcubes.service.weight.impl;

import fitcubes.dto.dashboard.WeightProgressResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.user.User;
import fitcubes.model.weightlog.WeightLog;
import fitcubes.repository.UserRepository;
import fitcubes.repository.WeightLogRepository;
import fitcubes.service.weight.WeightProgressService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeightProgressServiceImpl implements WeightProgressService {

    private static final int SCALE = 2;

    private final WeightLogRepository weightLogRepository;
    private final UserRepository userRepository;

    @Override
    public WeightProgressResponseDto getWeightProgress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        BigDecimal startingWeight = weightLogRepository.findEarliestByUserId(userId)
                .map(WeightLog::getWeight)
                .orElseGet(() -> fallbackWeight(user));

        BigDecimal currentWeight = weightLogRepository.findLatestByUserId(userId)
                .map(WeightLog::getWeight)
                .orElseGet(() -> fallbackWeight(user));

        BigDecimal targetWeight = BigDecimal.valueOf(user.getTargetWeight());

        BigDecimal totalChange = currentWeight.subtract(startingWeight)
                .setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal remainingToGoal = currentWeight.subtract(targetWeight)
                .setScale(SCALE, RoundingMode.HALF_UP);

        return new WeightProgressResponseDto(
                startingWeight, currentWeight, targetWeight, totalChange, remainingToGoal);
    }

    private BigDecimal fallbackWeight(User user) {
        if (user.getCurrentWeight() == null) {
            throw new IllegalStateException(
                    "No weight data available for user: " + user.getId());
        }
        return BigDecimal.valueOf(user.getCurrentWeight());
    }
}
