package fitcubes.service.exercise;

import fitcubes.dto.exercise.ExerciseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExerciseService {

    Page<ExerciseDto> getAllExercises(Pageable pageable);

    Page<ExerciseDto> searchExercises(String query, Pageable pageable);
}
