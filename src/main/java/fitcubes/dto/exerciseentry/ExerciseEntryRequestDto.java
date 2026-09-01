package fitcubes.dto.exerciseentry;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public record ExerciseEntryRequestDto(
        @NotNull Long exerciseId,
        @NotNull @Positive BigDecimal durationMinutes,
        @NotNull @PastOrPresent Instant loggedAt
) {
}
