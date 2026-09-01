package fitcubes.service.weight;

import fitcubes.dto.weightlog.WeightLogRequestDto;
import fitcubes.dto.weightlog.WeightLogResponseDto;
import java.math.BigDecimal;

public interface WeightLogService {

    BigDecimal getLatestWeight(Long userId);

    WeightLogResponseDto addWeightLog(Long userId, WeightLogRequestDto requestDto);
}
