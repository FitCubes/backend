package fitcubes.controller;

import fitcubes.dto.exerciseentry.ExerciseEntryRequestDto;
import fitcubes.dto.exerciseentry.ExerciseEntryResponseDto;
import fitcubes.model.user.User;
import fitcubes.service.exerciseentry.ExerciseEntryService;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/exercise-entries")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ExerciseEntryController {

    private final ExerciseEntryService exerciseEntryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ExerciseEntryResponseDto addExerciseEntry(
            @RequestBody @Valid ExerciseEntryRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        return exerciseEntryService.addExerciseEntry(user.getId(), requestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{entryId}")
    public ExerciseEntryResponseDto updateExerciseEntry(@PathVariable Long entryId,
                                                        @AuthenticationPrincipal User user,
                                                        @Valid @RequestBody
                                                        ExerciseEntryRequestDto requestDto) {
        return exerciseEntryService.updateExerciseEntry(user.getId(), entryId, requestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{entryId}")
    public void deleteExerciseEntry(@PathVariable Long entryId,
                                    @AuthenticationPrincipal User user) {
        exerciseEntryService.deleteExerciseEntry(user.getId(), entryId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{entryId}")
    public ExerciseEntryResponseDto getExerciseEntry(@PathVariable Long entryId,
                                                     @AuthenticationPrincipal User user) {
        return exerciseEntryService.getExerciseEntry(user.getId(), entryId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<ExerciseEntryResponseDto> getExerciseEntries(@RequestParam Instant from,
                                                             @RequestParam Instant to,
                                                             @ParameterObject Pageable pageable,
                                                             @AuthenticationPrincipal User user) {
        return exerciseEntryService.getExerciseEntries(user.getId(), from, to, pageable);
    }
}
