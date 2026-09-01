package fitcubes.service.exerciseentry.impl;

import fitcubes.dto.exerciseentry.ExerciseEntryRequestDto;
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.ExerciseEntryMapper;
import fitcubes.model.exercise.Exercise;
import fitcubes.model.exerciseentry.ExerciseEntry;
import fitcubes.repository.ExerciseEntryRepository;
import fitcubes.repository.ExerciseRepository;
import fitcubes.service.exerciseentry.ExerciseCalculationService;
import fitcubes.service.exerciseentry.ExerciseEntryService;
import fitcubes.service.weight.WeightLogService;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseEntryServiceImpl implements ExerciseEntryService {

    private final ExerciseEntryRepository exerciseEntryRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseEntryMapper exerciseEntryMapper;
    private final ExerciseCalculationService calculationService;
    private final WeightLogService weightLogService;

    @Override
    @Transactional
    public ExerciseEntryResponseDto addExerciseEntry(Long userId,
                                                     ExerciseEntryRequestDto requestDto) {
        ExerciseEntry entry = exerciseEntryMapper.toEntity(requestDto);
        entry.setUserId(userId);

        applyExercise(entry, userId, requestDto);

        ExerciseEntry saved = exerciseEntryRepository.save(entry);
        return exerciseEntryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ExerciseEntryResponseDto updateExerciseEntry(Long userId, Long entryId,
                                                        ExerciseEntryRequestDto requestDto) {
        ExerciseEntry entry = exerciseEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Exercise entry not found: " + entryId));

        entry.setDurationMinutes(requestDto.durationMinutes());
        entry.setLoggedAt(requestDto.loggedAt());

        applyExercise(entry, userId, requestDto);

        ExerciseEntry saved = exerciseEntryRepository.save(entry);
        return exerciseEntryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteExerciseEntry(Long userId, Long entryId) {
        ExerciseEntry entry = exerciseEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Exercise entry not found: " + entryId));

        exerciseEntryRepository.delete(entry);
    }

    @Override
    public ExerciseEntryResponseDto getExerciseEntry(Long userId, Long entryId) {
        ExerciseEntry entry = exerciseEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Exercise entry not found: " + entryId));

        return exerciseEntryMapper.toDto(entry);
    }

    @Override
    public Page<ExerciseEntryResponseDto> getExerciseEntries(Long userId, Instant from,
                                                             Instant to, Pageable pageable) {
        return exerciseEntryRepository
                .findByUserIdAndLoggedAtBetween(userId, from, to, pageable)
                .map(exerciseEntryMapper::toDto);
    }

    private void applyExercise(ExerciseEntry entry, Long userId,
                               ExerciseEntryRequestDto requestDto) {
        Exercise exercise = exerciseRepository.findById(requestDto.exerciseId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Exercise not found: " + requestDto.exerciseId()));

        BigDecimal weightKg = weightLogService.getLatestWeight(userId);

        entry.setExerciseId(exercise.getId());
        entry.setNameSnapshot(exercise.getName());
        entry.setCaloriesBurned(calculationService.calculateCaloriesBurned(
                exercise, weightKg, requestDto.durationMinutes()));
    }
}
