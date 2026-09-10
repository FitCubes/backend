package fitcubes.controller;

import fitcubes.dto.dashboard.WeightProgressResponseDto;
import fitcubes.dto.weightlog.WeightLogRequestDto;
import fitcubes.dto.weightlog.WeightLogResponseDto;
import fitcubes.model.user.User;
import fitcubes.service.weight.WeightLogService;
import fitcubes.service.weight.WeightProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/weight-logs")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class WeightLogController {

    private final WeightLogService weightLogService;
    private final WeightProgressService weightProgressService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public WeightLogResponseDto addWeightLog(@RequestBody @Valid WeightLogRequestDto requestDto,
                                             @AuthenticationPrincipal User user) {
        return weightLogService.addWeightLog(user.getId(), requestDto);
    }

    @GetMapping("/progress")
    public WeightProgressResponseDto getWeightProgress(@AuthenticationPrincipal User user) {
        return weightProgressService.getWeightProgress(user.getId());
    }
}
