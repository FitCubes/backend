package fitcubes.service.weight.impl;

import fitcubes.dto.weightlog.WeightLogRequestDto;
import fitcubes.dto.weightlog.WeightLogResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.user.User;
import fitcubes.model.weightlog.WeightLog;
import fitcubes.repository.UserRepository;
import fitcubes.repository.WeightLogRepository;
import fitcubes.service.weight.WeightLogService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeightLogServiceImpl implements WeightLogService {

    private final WeightLogRepository weightLogRepository;
    private final UserRepository userRepository;

    @Override
    public BigDecimal getLatestWeight(Long userId) {
        return weightLogRepository.findLatestByUserId(userId)
                .map(WeightLog::getWeight)
                .orElseGet(() -> fallbackToUserProfile(userId));
    }

    @Override
    @Transactional
    public WeightLogResponseDto addWeightLog(Long userId, WeightLogRequestDto requestDto) {
        WeightLog log = new WeightLog();
        log.setUserId(userId);
        log.setWeight(requestDto.weight());
        log.setLoggedAt(requestDto.loggedAt());

        WeightLog saved = weightLogRepository.save(log);

        return new WeightLogResponseDto(saved.getId(), saved.getWeight(), saved.getLoggedAt());
    }

    private BigDecimal fallbackToUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (user.getCurrentWeight() == null) {
            throw new IllegalStateException(
                    "No weight data available for user: " + userId);
        }
        return BigDecimal.valueOf(user.getCurrentWeight());
    }
}
