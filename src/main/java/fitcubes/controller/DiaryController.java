package fitcubes.controller;

import fitcubes.dto.diary.DiaryExerciseEntryDto;
import fitcubes.dto.diary.DiaryFoodEntryDto;
import fitcubes.dto.diary.DiaryResponseDto;
import fitcubes.dto.exerciseentry.ExerciseEntryRequestDto;
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import fitcubes.dto.foodentry.FoodEntryRequestDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import fitcubes.model.user.User;
import fitcubes.service.diary.DiaryService;
import fitcubes.service.exerciseentry.ExerciseEntryService;
import fitcubes.service.foodentry.FoodEntryService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/diary")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class DiaryController {

    private final DiaryService diaryService;
    private final FoodEntryService foodEntryService;
    private final ExerciseEntryService exerciseEntryService;

    @GetMapping("/{date}")
    public DiaryResponseDto getDiary(@PathVariable LocalDate date,
                                     @AuthenticationPrincipal User user) {
        return diaryService.getDiary(user.getId(), date);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{date}/food")
    public DiaryFoodEntryDto addFoodEntry(@PathVariable LocalDate date,
                                          @Valid @RequestBody FoodEntryRequestDto requestDto,
                                          @AuthenticationPrincipal User user) {
        FoodEntryResponseDto saved = foodEntryService.addFoodEntry(user.getId(), requestDto);
        return new DiaryFoodEntryDto(
                saved.id(), saved.nameSnapshot(), saved.calories(), saved.protein(),
                saved.carbs(), saved.fat(), saved.quantity(),
                saved.mealType().name().toLowerCase()
        );
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{date}/food/{entryId}")
    public void deleteFoodEntry(@PathVariable LocalDate date,
                                @PathVariable Long entryId,
                                @AuthenticationPrincipal User user) {
        foodEntryService.deleteFoodEntry(user.getId(), entryId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{date}/exercise")
    public DiaryExerciseEntryDto addExerciseEntry(@PathVariable LocalDate date,
                                                  @Valid @RequestBody
                                                  ExerciseEntryRequestDto requestDto,
                                                  @AuthenticationPrincipal User user) {
        ExerciseEntryResponseDto saved =
                exerciseEntryService.addExerciseEntry(user.getId(), requestDto);
        return new DiaryExerciseEntryDto(
                saved.id(), saved.nameSnapshot(), saved.caloriesBurned(), saved.durationMinutes()
        );
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{date}/exercise/{entryId}")
    public void deleteExerciseEntry(@PathVariable LocalDate date,
                                    @PathVariable Long entryId,
                                    @AuthenticationPrincipal User user) {
        exerciseEntryService.deleteExerciseEntry(user.getId(), entryId);
    }
}
