package fitcubes.service.diary;

import fitcubes.dto.diary.DiaryExerciseEntryDto;
import fitcubes.dto.diary.DiaryFoodEntryDto;
import fitcubes.dto.diary.DiaryResponseDto;
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import fitcubes.service.exerciseentry.ExerciseEntryService;
import fitcubes.service.foodentry.FoodEntryService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final FoodEntryService foodEntryService;
    private final ExerciseEntryService exerciseEntryService;

    @Override
    public DiaryResponseDto getDiary(Long userId, LocalDate date) {
        Instant from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusMillis(1);

        List<DiaryFoodEntryDto> foodEntries = foodEntryService
                .getFoodEntries(userId, from, to, Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::toDiaryFoodEntry)
                .toList();

        List<DiaryExerciseEntryDto> exerciseEntries = exerciseEntryService
                .getExerciseEntries(userId, from, to, Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::toDiaryExerciseEntry)
                .toList();

        return new DiaryResponseDto(date, foodEntries, exerciseEntries);
    }

    private DiaryFoodEntryDto toDiaryFoodEntry(FoodEntryResponseDto entry) {
        return new DiaryFoodEntryDto(
                entry.id(),
                entry.nameSnapshot(),
                entry.calories(),
                entry.protein(),
                entry.carbs(),
                entry.fat(),
                entry.quantity(),
                entry.mealType().name().toLowerCase()
        );
    }

    private DiaryExerciseEntryDto toDiaryExerciseEntry(ExerciseEntryResponseDto entry) {
        return new DiaryExerciseEntryDto(
                entry.id(),
                entry.nameSnapshot(),
                entry.caloriesBurned(),
                entry.durationMinutes()
        );
    }
}
