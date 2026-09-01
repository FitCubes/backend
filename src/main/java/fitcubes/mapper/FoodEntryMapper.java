package fitcubes.mapper;

import fitcubes.dto.foodentry.FoodEntryRequestDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import fitcubes.model.foodentry.FoodEntry;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FoodEntryMapper {

    FoodEntry toEntity(FoodEntryRequestDto foodEntryRequestDto);

    FoodEntryResponseDto toDto(FoodEntry foodEntry);
}
