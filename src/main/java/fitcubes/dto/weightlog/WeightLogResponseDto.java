package fitcubes.dto.weightlog;

import java.math.BigDecimal;
import java.time.Instant;

public record WeightLogResponseDto(
        Long id,
        BigDecimal weight,
        Instant loggedAt
) {
}
