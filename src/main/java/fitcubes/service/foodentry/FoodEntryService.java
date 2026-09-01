package fitcubes.service.foodentry;

import fitcubes.dto.foodentry.FoodEntryRequestDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FoodEntryService {

    FoodEntryResponseDto addFoodEntry(Long userId, FoodEntryRequestDto requestDto);

    FoodEntryResponseDto updateFoodEntry(Long userId, Long entryId, FoodEntryRequestDto requestDto);

    void deleteFoodEntry(Long userId, Long entryId);

    FoodEntryResponseDto getFoodEntry(Long userId, Long entryId);

    Page<FoodEntryResponseDto> getFoodEntries(
            Long userId, Instant from, Instant to, Pageable pageable);
}
