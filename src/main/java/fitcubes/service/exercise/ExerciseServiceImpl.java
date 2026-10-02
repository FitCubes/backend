package fitcubes.service.exercise;

import fitcubes.dto.exercise.ExerciseDto;
import fitcubes.model.exercise.Exercise;
import fitcubes.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;

    @Override
    public Page<ExerciseDto> getAllExercises(Pageable pageable) {
        return exerciseRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public Page<ExerciseDto> searchExercises(String query, Pageable pageable) {
        return exerciseRepository.searchByName(query, pageable).map(this::toDto);
    }

    private ExerciseDto toDto(Exercise exercise) {
        return new ExerciseDto(exercise.getId(), exercise.getName(), exercise.getCategory(),
                exercise.getPrimaryMuscles(), exercise.getMet(), exercise.getUnit(),
                exercise.getCaloriesPerUnit());
    }
}
