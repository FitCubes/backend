package fitcubes.mapper;

import fitcubes.dto.exerciseentry.ExerciseEntryRequestDto;
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import fitcubes.model.exerciseentry.ExerciseEntry;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExerciseEntryMapper {

    ExerciseEntry toEntity(ExerciseEntryRequestDto requestDto);

    ExerciseEntryResponseDto toDto(ExerciseEntry entry);
}
