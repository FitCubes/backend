package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fitcubes.dto.dashboard.WeightProgressResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.user.User;
import fitcubes.model.weightlog.WeightLog;
import fitcubes.repository.UserRepository;
import fitcubes.repository.WeightLogRepository;
import fitcubes.service.weight.impl.WeightProgressServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeightProgressServiceImplTest {

    @Mock
    private WeightLogRepository weightLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WeightProgressServiceImpl service;

    private static final Long USER_ID = 42L;

    private User buildUser(Double currentWeight, double targetWeight) {
        User user = new User();
        user.setId(USER_ID);
        user.setCurrentWeight(currentWeight);
        user.setTargetWeight(targetWeight);
        return user;
    }

    private WeightLog buildLog(double weight) {
        WeightLog log = new WeightLog();
        log.setWeight(BigDecimal.valueOf(weight));
        return log;
    }

    @Test
    void getWeightProgress_multipleLogs_computesCorrectChange() {
        User user = buildUser(80.0, 75.0);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(weightLogRepository.findEarliestByUserId(USER_ID))
                .thenReturn(Optional.of(buildLog(85.0)));
        when(weightLogRepository.findLatestByUserId(USER_ID))
                .thenReturn(Optional.of(buildLog(80.0)));

        WeightProgressResponseDto result = service.getWeightProgress(USER_ID);

        assertThat(result.startingWeight()).isEqualByComparingTo(BigDecimal.valueOf(85.0));
        assertThat(result.currentWeight()).isEqualByComparingTo(BigDecimal.valueOf(80.0));
        assertThat(result.targetWeight()).isEqualByComparingTo(BigDecimal.valueOf(75.0));
        // 80 - 85 = -5 (schudł 5kg)
        assertThat(result.totalChange()).isEqualByComparingTo(BigDecimal.valueOf(-5.00));
        // 80 - 75 = 5 (zostało 5kg do celu)
        assertThat(result.remainingToGoal()).isEqualByComparingTo(BigDecimal.valueOf(5.00));
    }

    @Test
    void getWeightProgress_weightGain_totalChangeIsPositive() {
        User user = buildUser(90.0, 85.0);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(weightLogRepository.findEarliestByUserId(USER_ID))
                .thenReturn(Optional.of(buildLog(85.0)));
        when(weightLogRepository.findLatestByUserId(USER_ID))
                .thenReturn(Optional.of(buildLog(90.0)));

        WeightProgressResponseDto result = service.getWeightProgress(USER_ID);

        // 90 - 85 = +5 (przytył 5kg)
        assertThat(result.totalChange()).isEqualByComparingTo(BigDecimal.valueOf(5.00));
    }

    @Test
    void getWeightProgress_noLogs_fallsBackToUserCurrentWeight_totalChangeIsZero() {
        User user = buildUser(80.0, 75.0);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(weightLogRepository.findEarliestByUserId(USER_ID)).thenReturn(Optional.empty());
        when(weightLogRepository.findLatestByUserId(USER_ID)).thenReturn(Optional.empty());

        WeightProgressResponseDto result = service.getWeightProgress(USER_ID);

        assertThat(result.startingWeight()).isEqualByComparingTo(BigDecimal.valueOf(80.0));
        assertThat(result.currentWeight()).isEqualByComparingTo(BigDecimal.valueOf(80.0));
        assertThat(result.totalChange()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getWeightProgress_singleLog_totalChangeIsZero() {
        User user = buildUser(80.0, 75.0);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(weightLogRepository.findEarliestByUserId(USER_ID))
                .thenReturn(Optional.of(buildLog(82.0)));
        when(weightLogRepository.findLatestByUserId(USER_ID))
                .thenReturn(Optional.of(buildLog(82.0)));

        WeightProgressResponseDto result = service.getWeightProgress(USER_ID);

        assertThat(result.totalChange()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getWeightProgress_userNotFound_throwsEntityNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getWeightProgress(USER_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getWeightProgress_noLogsAndNoCurrentWeight_throwsIllegalState() {
        User user = buildUser(null, 75.0);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(weightLogRepository.findEarliestByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getWeightProgress(USER_ID))
                .isInstanceOf(IllegalStateException.class);
    }
}
