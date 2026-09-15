package fitcubes.dto.diary;

import java.time.LocalDate;
import java.util.List;

public record DiaryResponseDto(
        LocalDate date,
        List<DiaryFoodEntryDto> foodEntries,
        List<DiaryExerciseEntryDto> exerciseEntries
) {
}
