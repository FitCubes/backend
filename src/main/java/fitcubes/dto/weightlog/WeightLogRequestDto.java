package fitcubes.dto.weightlog;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public record WeightLogRequestDto(
        @NotNull @Positive BigDecimal weight,
        @NotNull @PastOrPresent Instant loggedAt
) {
}
