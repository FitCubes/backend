package fitcubes.dto.dashboard;

import java.math.BigDecimal;

public record DailySummaryResponseDto(
        BigDecimal targetCalories,
        BigDecimal consumed,
        BigDecimal burned,
        BigDecimal remaining
) {
}
