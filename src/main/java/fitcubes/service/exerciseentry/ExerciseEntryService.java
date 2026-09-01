package fitcubes.service.exerciseentry;

import fitcubes.dto.exerciseentry.ExerciseEntryRequestDto;
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExerciseEntryService {

    ExerciseEntryResponseDto addExerciseEntry(Long userId, ExerciseEntryRequestDto requestDto);

    ExerciseEntryResponseDto updateExerciseEntry(Long userId, Long entryId,
                                                 ExerciseEntryRequestDto requestDto);

    void deleteExerciseEntry(Long userId, Long entryId);

    ExerciseEntryResponseDto getExerciseEntry(Long userId, Long entryId);

    Page<ExerciseEntryResponseDto> getExerciseEntries(Long userId, Instant from, Instant to,
                                                      Pageable pageable);
}
