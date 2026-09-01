package fitcubes.controller;

import fitcubes.dto.dashboard.DailySummaryResponseDto;
import fitcubes.dto.dashboard.PredictedWeightChangeResponseDto;
import fitcubes.model.user.User;
import fitcubes.service.dashboard.DailySummaryService;
import fitcubes.service.weight.WeightPredictionService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class DashboardController {

    private final DailySummaryService dailySummaryService;

    private final WeightPredictionService weightPredictionService;

    @GetMapping("/daily-summary")
    public DailySummaryResponseDto getDailySummary(@RequestParam Instant from,
                                                   @RequestParam Instant to,
                                                   @AuthenticationPrincipal User user) {
        return dailySummaryService.getDailySummary(user.getId(), from, to);
    }

    @GetMapping("/predicted-weight-change")
    public PredictedWeightChangeResponseDto getPredictedWeeklyChange(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @AuthenticationPrincipal User user) {
        return weightPredictionService.getPredictedWeeklyChange(user.getId(), from, to);
    }
}
