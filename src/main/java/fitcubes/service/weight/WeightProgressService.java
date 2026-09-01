package fitcubes.service.weight;

import fitcubes.dto.dashboard.WeightProgressResponseDto;

public interface WeightProgressService {

    WeightProgressResponseDto getWeightProgress(Long userId);
}
