package fitcubes.service.dashboard;

import fitcubes.dto.dashboard.DailySummaryResponseDto;
import java.time.Instant;

public interface DailySummaryService {

    DailySummaryResponseDto getDailySummary(Long userId, Instant from, Instant to);
}
