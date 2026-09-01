package fitcubes.dto.dashboard;

import java.math.BigDecimal;

public record WeightProgressResponseDto(
        BigDecimal startingWeight,
        BigDecimal currentWeight,
        BigDecimal targetWeight,
        BigDecimal totalChange,
        BigDecimal remainingToGoal
) {
}
