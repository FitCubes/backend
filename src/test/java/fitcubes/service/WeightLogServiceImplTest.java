package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fitcubes.dto.weightlog.WeightLogRequestDto;
import fitcubes.dto.weightlog.WeightLogResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.user.User;
import fitcubes.model.weightlog.WeightLog;
import fitcubes.repository.UserRepository;
import fitcubes.repository.WeightLogRepository;
import fitcubes.service.weight.impl.WeightLogServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeightLogServiceImplTest {

    @Mock
    private WeightLogRepository weightLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WeightLogServiceImpl weightLogService;

    private static final Long USER_ID = 42L;

    @Test
    void getLatestWeight_logExists_returnsLoggedWeight() {
        WeightLog log = new WeightLog();
        log.setWeight(BigDecimal.valueOf(75.5));

        when(weightLogRepository.findLatestByUserId(USER_ID)).thenReturn(Optional.of(log));

        BigDecimal result = weightLogService.getLatestWeight(USER_ID);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(75.5));
    }

    @Test
    void getLatestWeight_noLog_fallsBackToUserCurrentWeight() {
        User user = new User();
        user.setId(USER_ID);
        user.setCurrentWeight(80.0);

        when(weightLogRepository.findLatestByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        BigDecimal result = weightLogService.getLatestWeight(USER_ID);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(80.0));
    }

    @Test
    void getLatestWeight_noLogAndUserNotFound_throwsEntityNotFound() {
        when(weightLogRepository.findLatestByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> weightLogService.getLatestWeight(USER_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getLatestWeight_noLogAndNoCurrentWeight_throwsIllegalState() {
        User user = new User();
        user.setId(USER_ID);
        user.setCurrentWeight(null);

        when(weightLogRepository.findLatestByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> weightLogService.getLatestWeight(USER_ID))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void addWeightLog_savesWithCorrectFields() {
        Instant loggedAt = Instant.now();
        WeightLogRequestDto requestDto = new WeightLogRequestDto(BigDecimal.valueOf(74.2), loggedAt);

        WeightLog savedEntity = new WeightLog();
        savedEntity.setId(1L);
        savedEntity.setUserId(USER_ID);
        savedEntity.setWeight(BigDecimal.valueOf(74.2));
        savedEntity.setLoggedAt(loggedAt);

        ArgumentCaptor<WeightLog> captor = ArgumentCaptor.forClass(WeightLog.class);
        when(weightLogRepository.save(any())).thenReturn(savedEntity);

        WeightLogResponseDto result = weightLogService.addWeightLog(USER_ID, requestDto);

        verify(weightLogRepository).save(captor.capture());
        WeightLog toSave = captor.getValue();
        assertThat(toSave.getUserId()).isEqualTo(USER_ID);
        assertThat(toSave.getWeight()).isEqualByComparingTo(BigDecimal.valueOf(74.2));
        assertThat(toSave.getLoggedAt()).isEqualTo(loggedAt);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.weight()).isEqualByComparingTo(BigDecimal.valueOf(74.2));
    }
}
