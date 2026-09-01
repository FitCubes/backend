package fitcubes.service.weight;

import fitcubes.dto.dashboard.PredictedWeightChangeResponseDto;
import java.time.Instant;

public interface WeightPredictionService {

    PredictedWeightChangeResponseDto getPredictedWeeklyChange(Long userId, Instant from,
                                                              Instant to);
}
