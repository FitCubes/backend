package fitcubes.service.diary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import fitcubes.dto.exercise.ExerciseDto;
import fitcubes.model.exercise.Exercise;
import fitcubes.model.exercise.ExerciseCategory;
import fitcubes.repository.ExerciseRepository;
import fitcubes.service.exercise.impl.ExerciseServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = ExerciseServiceImpl.class)
class ExerciseServiceImplTest {

    private static final Long EXERCISE_ID = 1L;
    private static final String EXERCISE_NAME = "Running";

    @MockitoBean
    private ExerciseRepository exerciseRepository;

    @Autowired
    private ExerciseServiceImpl exerciseService;

    private Exercise exercise;

    @BeforeEach
    void setUp() {
        exercise = new Exercise();
        exercise.setId(EXERCISE_ID);
        exercise.setName(EXERCISE_NAME);
        exercise.setCategory(ExerciseCategory.CARDIO);
        exercise.setPrimaryMuscles("Legs");
        exercise.setMet(BigDecimal.valueOf(8));
    }

    @Nested
    @DisplayName("getAllExercises")
    class GetAllExercises {

        @Test
        @DisplayName("getAllExercises_returnsMappedPage")
        void getAllExercises_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise));
            given(exerciseRepository.findAll(pageable)).willReturn(exercisePage);

            Page<ExerciseDto> result = exerciseService.getAllExercises(pageable);

            assertThat(result.getContent()).hasSize(1);
            ExerciseDto dto = result.getContent().get(0);
            assertThat(dto.id()).isEqualTo(EXERCISE_ID);
            assertThat(dto.name()).isEqualTo(EXERCISE_NAME);
            assertThat(dto.category()).isEqualTo(ExerciseCategory.CARDIO);
            assertThat(dto.primaryMuscles()).isEqualTo("Legs");
            assertThat(dto.met()).isEqualByComparingTo(BigDecimal.valueOf(8));
        }

        @Test
        @DisplayName("getAllExercises_noExercises_returnsEmptyPage")
        void getAllExercises_noExercises_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            given(exerciseRepository.findAll(pageable)).willReturn(Page.empty(pageable));

            Page<ExerciseDto> result = exerciseService.getAllExercises(pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchExercises")
    class SearchExercises {

        @Test
        @DisplayName("searchExercises_matchingQuery_returnsMappedPage")
        void searchExercises_matchingQuery_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise));
            given(exerciseRepository.searchByName(eq("run"), eq(pageable)))
                    .willReturn(exercisePage);

            Page<ExerciseDto> result = exerciseService.searchExercises("run", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo(EXERCISE_NAME);
        }

        @Test
        @DisplayName("searchExercises_noMatch_returnsEmptyPage")
        void searchExercises_noMatch_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            given(exerciseRepository.searchByName(eq("xyz"), eq(pageable)))
                    .willReturn(Page.empty(pageable));

            Page<ExerciseDto> result = exerciseService.searchExercises("xyz", pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }
}
