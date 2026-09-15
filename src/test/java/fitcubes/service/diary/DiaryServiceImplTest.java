package fitcubes.service.diary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fitcubes.dto.diary.DiaryResponseDto;
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import fitcubes.model.foodentry.MealType;
import fitcubes.model.foodentry.SourceType;
import fitcubes.service.exerciseentry.ExerciseEntryService;
import fitcubes.service.foodentry.FoodEntryService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DiaryServiceImplTest {

    @Mock
    private FoodEntryService foodEntryService;

    @Mock
    private ExerciseEntryService exerciseEntryService;

    @InjectMocks
    private DiaryServiceImpl diaryService;

    private static final Long USER_ID = 42L;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 14);

    @Test
    void getDiary_returnsMappedFoodAndExerciseEntries() {
        FoodEntryResponseDto food = new FoodEntryResponseDto(
                1L, SourceType.PRODUCT, 5L, null, "Chicken Breast",
                BigDecimal.valueOf(100), BigDecimal.valueOf(165), BigDecimal.valueOf(31),
                BigDecimal.ZERO, BigDecimal.valueOf(3.6), MealType.LUNCH, Instant.now()
        );
        ExerciseEntryResponseDto exercise = new ExerciseEntryResponseDto(
                2L, 7L, "Running", BigDecimal.valueOf(30), BigDecimal.valueOf(320), Instant.now()
        );

        when(foodEntryService.getFoodEntries(eq(USER_ID), any(), any(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(food)));
        when(exerciseEntryService.getExerciseEntries(
                eq(USER_ID), any(), any(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(exercise)));

        DiaryResponseDto result = diaryService.getDiary(USER_ID, DATE);

        assertThat(result.date()).isEqualTo(DATE);
        assertThat(result.foodEntries()).hasSize(1);
        assertThat(result.foodEntries().get(0).id()).isEqualTo(1L);
        assertThat(result.foodEntries().get(0).name()).isEqualTo("Chicken Breast");
        assertThat(result.foodEntries().get(0).weightGrams())
                .isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.foodEntries().get(0).mealType()).isEqualTo("lunch");

        assertThat(result.exerciseEntries()).hasSize(1);
        assertThat(result.exerciseEntries().get(0).id()).isEqualTo(2L);
        assertThat(result.exerciseEntries().get(0).name()).isEqualTo("Running");
        assertThat(result.exerciseEntries().get(0).caloriesBurned())
                .isEqualByComparingTo(BigDecimal.valueOf(320));
    }

    @Test
    void getDiary_noEntries_returnsEmptyLists() {
        when(foodEntryService.getFoodEntries(eq(USER_ID), any(), any(), eq(Pageable.unpaged())))
                .thenReturn(Page.empty());
        when(exerciseEntryService.getExerciseEntries(
                eq(USER_ID), any(), any(), eq(Pageable.unpaged())))
                .thenReturn(Page.empty());

        DiaryResponseDto result = diaryService.getDiary(USER_ID, DATE);

        assertThat(result.foodEntries()).isEmpty();
        assertThat(result.exerciseEntries()).isEmpty();
    }

    @Test
    void getDiary_usesCorrectDateRangeForWholeDay() {
        when(foodEntryService.getFoodEntries(eq(USER_ID), any(), any(), eq(Pageable.unpaged())))
                .thenReturn(Page.empty());
        when(exerciseEntryService.getExerciseEntries(
                eq(USER_ID), any(), any(), eq(Pageable.unpaged())))
                .thenReturn(Page.empty());

        diaryService.getDiary(USER_ID, DATE);

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(foodEntryService).getFoodEntries(
                eq(USER_ID), fromCaptor.capture(), toCaptor.capture(), eq(Pageable.unpaged()));

        Instant expectedFrom = DATE.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant expectedTo = DATE.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                .minusMillis(1);

        assertThat(fromCaptor.getValue()).isEqualTo(expectedFrom);
        assertThat(toCaptor.getValue()).isEqualTo(expectedTo);
    }
}
