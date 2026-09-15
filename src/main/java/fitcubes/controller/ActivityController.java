package fitcubes.controller;

import fitcubes.dto.exercise.ExerciseDto;
import fitcubes.service.exercise.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/activities")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ActivityController {

    private final ExerciseService exerciseService;

    @GetMapping
    public Page<ExerciseDto> getAllActivities(@ParameterObject Pageable pageable) {
        return exerciseService.getAllExercises(pageable);
    }

    @GetMapping("/search")
    public Page<ExerciseDto> searchActivities(@RequestParam String query,
                                              @ParameterObject Pageable pageable) {
        return exerciseService.searchExercises(query, pageable);
    }
}
