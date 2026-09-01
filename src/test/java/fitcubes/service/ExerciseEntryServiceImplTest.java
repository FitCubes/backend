package fitcubes.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fitcubes.dto.exerciseentry.ExerciseEntryRequestDto;
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.ExerciseEntryMapper;
import fitcubes.model.exercise.Exercise;
import fitcubes.model.exerciseentry.ExerciseEntry;
import fitcubes.repository.ExerciseEntryRepository;
import fitcubes.repository.ExerciseRepository;
import fitcubes.service.exerciseentry.ExerciseCalculationService;
import fitcubes.service.weight.WeightLogService;
import fitcubes.service.exerciseentry.impl.ExerciseEntryServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ExerciseEntryServiceImplTest {

    @Mock
    private ExerciseEntryRepository exerciseEntryRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseEntryMapper exerciseEntryMapper;

    @Mock
    private ExerciseCalculationService calculationService;

    @Mock
    private WeightLogService weightLogService;

    @InjectMocks
    private ExerciseEntryServiceImpl service;

    private static final Long USER_ID = 42L;
    private static final Long ENTRY_ID = 10L;

    private ExerciseEntryRequestDto request() {
        return new ExerciseEntryRequestDto(1L, BigDecimal.valueOf(30), Instant.now());
    }

    @Test
    void addExerciseEntry_savesAndReturnsDto() {
        ExerciseEntryRequestDto request = request();
        Exercise running = mock(Exercise.class);
        when(running.getId()).thenReturn(1L);
        when(running.getName()).thenReturn("Running");

        ExerciseEntry mappedEntity = new ExerciseEntry();
        ExerciseEntry savedEntity = new ExerciseEntry();
        ExerciseEntryResponseDto expectedDto = mock(ExerciseEntryResponseDto.class);

        when(exerciseEntryMapper.toEntity(request)).thenReturn(mappedEntity);
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(running));
        when(weightLogService.getLatestWeight(USER_ID)).thenReturn(BigDecimal.valueOf(70));
        when(calculationService.calculateCaloriesBurned(running, BigDecimal.valueOf(70),
                request.durationMinutes())).thenReturn(BigDecimal.valueOf(294));
        when(exerciseEntryRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(exerciseEntryMapper.toDto(savedEntity)).thenReturn(expectedDto);

        ExerciseEntryResponseDto result = service.addExerciseEntry(USER_ID, request);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(mappedEntity.getUserId()).isEqualTo(USER_ID);
        assertThat(mappedEntity.getExerciseId()).isEqualTo(1L);
        assertThat(mappedEntity.getNameSnapshot()).isEqualTo("Running");
        assertThat(mappedEntity.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(294));
        verify(exerciseEntryRepository).save(mappedEntity);
    }

    @Test
    void addExerciseEntry_exerciseNotFound_throwsEntityNotFound() {
        ExerciseEntryRequestDto request = request();
        when(exerciseEntryMapper.toEntity(request)).thenReturn(new ExerciseEntry());
        when(exerciseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addExerciseEntry(USER_ID, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(exerciseEntryRepository, never()).save(any());
    }

    @Test
    void updateExerciseEntry_notFound_throwsEntityNotFound() {
        ExerciseEntryRequestDto request = request();
        when(exerciseEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateExerciseEntry(USER_ID, ENTRY_ID, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(exerciseEntryRepository, never()).save(any());
    }

    @Test
    void updateExerciseEntry_existingEntry_updatesAndReturnsDto() {
        ExerciseEntryRequestDto request = request();
        ExerciseEntry existingEntry = new ExerciseEntry();
        Exercise running = mock(Exercise.class);
        when(running.getId()).thenReturn(1L);
        when(running.getName()).thenReturn("Running");
        ExerciseEntryResponseDto expectedDto = mock(ExerciseEntryResponseDto.class);

        when(exerciseEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID))
                .thenReturn(Optional.of(existingEntry));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(running));
        when(weightLogService.getLatestWeight(USER_ID)).thenReturn(BigDecimal.valueOf(70));
        when(calculationService.calculateCaloriesBurned(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(200));
        when(exerciseEntryRepository.save(existingEntry)).thenReturn(existingEntry);
        when(exerciseEntryMapper.toDto(existingEntry)).thenReturn(expectedDto);

        ExerciseEntryResponseDto result = service.updateExerciseEntry(USER_ID, ENTRY_ID, request);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(existingEntry.getDurationMinutes())
                .isEqualByComparingTo(request.durationMinutes());
        verify(exerciseEntryMapper, never()).toEntity(any());
    }

    @Test
    void deleteExerciseEntry_existingEntry_deletesIt() {
        ExerciseEntry entry = new ExerciseEntry();
        when(exerciseEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID))
                .thenReturn(Optional.of(entry));

        service.deleteExerciseEntry(USER_ID, ENTRY_ID);

        verify(exerciseEntryRepository, times(1)).delete(entry);
    }

    @Test
    void deleteExerciseEntry_notFound_throwsEntityNotFound() {
        when(exerciseEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteExerciseEntry(USER_ID, ENTRY_ID))
                .isInstanceOf(EntityNotFoundException.class);

        verify(exerciseEntryRepository, never()).delete(any());
    }

    @Test
    void getExerciseEntry_existingEntry_returnsDto() {
        ExerciseEntry entry = new ExerciseEntry();
        ExerciseEntryResponseDto expectedDto = mock(ExerciseEntryResponseDto.class);

        when(exerciseEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID))
                .thenReturn(Optional.of(entry));
        when(exerciseEntryMapper.toDto(entry)).thenReturn(expectedDto);

        ExerciseEntryResponseDto result = service.getExerciseEntry(USER_ID, ENTRY_ID);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void getExerciseEntry_notFound_throwsEntityNotFound() {
        when(exerciseEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExerciseEntry(USER_ID, ENTRY_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getExerciseEntries_returnsMappedPage() {
        Instant from = Instant.parse("2026-08-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-31T23:59:59Z");
        Pageable pageable = PageRequest.of(0, 20);

        ExerciseEntry entry = new ExerciseEntry();
        ExerciseEntryResponseDto dto = mock(ExerciseEntryResponseDto.class);
        Page<ExerciseEntry> entityPage = new PageImpl<>(List.of(entry), pageable, 1);

        when(exerciseEntryRepository.findByUserIdAndLoggedAtBetween(USER_ID, from, to, pageable))
                .thenReturn(entityPage);
        when(exerciseEntryMapper.toDto(entry)).thenReturn(dto);

        Page<ExerciseEntryResponseDto> result =
                service.getExerciseEntries(USER_ID, from, to, pageable);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
