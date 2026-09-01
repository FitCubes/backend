package fitcubes.dto.dashboard;

import java.math.BigDecimal;

public record PredictedWeightChangeResponseDto(
        BigDecimal averageDailyDeficit,
        BigDecimal predictedWeeklyChangeKg
) {
}
