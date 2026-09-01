package fitcubes.controller;

import fitcubes.dto.foodentry.FoodEntryRequestDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import fitcubes.model.user.User;
import fitcubes.service.FoodEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/food-entries")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@Tag(name = "Food Entry Management",
        description = "Endpoints for managing user's food log entries")
public class FoodEntryController {

    private final FoodEntryService foodEntryService;

    @Operation(
            summary = "Add food entry",
            description = "Logs a new food entry for the user. Source can be a product, "
                    + "a recipe, or a custom entry.",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Food entry created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "404",
                            description = "Referenced product or recipe not found")
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public FoodEntryResponseDto addFoodEntry(@RequestBody @Valid FoodEntryRequestDto requestDto,
                                             @AuthenticationPrincipal User user) {
        return foodEntryService.addFoodEntry(user.getId(), requestDto);
    }

    @Operation(
            summary = "Update food entry",
            description = "Updates an existing food entry. "
                    + "Users can only modify entries created by themselves.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Food entry updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "404",
                            description = "Food entry not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{entryId}")
    public FoodEntryResponseDto updateFoodEntry(
            @PathVariable Long entryId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FoodEntryRequestDto requestDto) {
        return foodEntryService.updateFoodEntry(user.getId(), entryId, requestDto);
    }

    @Operation(
            summary = "Delete food entry",
            description = "Deletes a food entry by its ID. "
                    + "Users can only delete entries created by themselves.",
            responses = {
                    @ApiResponse(responseCode = "204",
                            description = "Food entry deleted successfully"),
                    @ApiResponse(responseCode = "404",
                            description = "Food entry not found")
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{entryId}")
    public void deleteFoodEntry(@PathVariable Long entryId, @AuthenticationPrincipal User user) {
        foodEntryService.deleteFoodEntry(user.getId(), entryId);
    }

    @Operation(
            summary = "Get food entry by ID",
            description = "Retrieves a single food entry by its ID. "
                    + "Users can only access entries created by themselves.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Food entry retrieved successfully"),
                    @ApiResponse(responseCode = "404",
                            description = "Food entry not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{entryId}")
    public FoodEntryResponseDto getFoodEntry(@PathVariable Long entryId,
                                             @AuthenticationPrincipal User user) {
        return foodEntryService.getFoodEntry(user.getId(), entryId);
    }

    @Operation(
            summary = "Get food entries",
            description = "Retrieves a paginated list of the user's food entries "
                    + "within a given time range.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Food entries retrieved successfully")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<FoodEntryResponseDto> getFoodEntries(@RequestParam Instant from,
                                                     @RequestParam Instant to,
                                                     @ParameterObject Pageable pageable,
                                                     @AuthenticationPrincipal User user) {
        return foodEntryService.getFoodEntries(user.getId(), from, to, pageable);
    }
}
