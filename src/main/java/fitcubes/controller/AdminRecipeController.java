package fitcubes.controller;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import fitcubes.service.recipe.AdminRecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/recipes")
@Tag(name = "Admin Recipe Management",
        description = "Endpoints for managing global system recipes")
public class AdminRecipeController {

    private final AdminRecipeService adminRecipeService;

    @Operation(
            summary = "Create global system recipe",
            description = "Creates a new recipe available to all users in the default dictionary.",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Global recipe created successfully"),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid payload provided"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required")
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RecipeDto createRecipe(@Valid @RequestBody CreateRecipeDto createRecipeDto) {
        return adminRecipeService.save(createRecipeDto);
    }

    @Operation(
            summary = "Get all recipes (Admin)",
            description = "Retrieves a paginated list of all recipes, "
                    + "including both global and user-owned recipes.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Recipes retrieved successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<RecipeDto> getAllRecipes(@ParameterObject Pageable pageable) {
        return adminRecipeService.getAllRecipes(pageable);
    }

    @Operation(
            summary = "Get recipe by ID (Admin)",
            description = "Retrieves any recipe by its ID regardless of ownership.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Recipe retrieved successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required"),
                    @ApiResponse(responseCode = "404",
                            description = "Recipe not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{recipeId}")
    public RecipeDto getRecipeById(@PathVariable Long recipeId) {
        return adminRecipeService.getRecipeById(recipeId);
    }

    @Operation(
            summary = "Update global or user recipe (Admin)",
            description = "Updates any recipe by its ID without owner restrictions.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Recipe updated successfully"),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid payload provided"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required"),
                    @ApiResponse(responseCode = "404",
                            description = "Recipe not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{recipeId}")
    public RecipeDto updateRecipe(@PathVariable Long recipeId,
                                  @RequestBody @Valid UpdateRecipeDto updateRecipeDto) {
        return adminRecipeService.update(updateRecipeDto, recipeId);
    }

    @Operation(
            summary = "Delete global or user recipe (Admin)",
            description = "Deletes any recipe by its ID without owner restrictions.",
            responses = {
                    @ApiResponse(responseCode = "204",
                            description = "Recipe deleted successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required"),
                    @ApiResponse(responseCode = "404",
                            description = "Recipe not found")
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{recipeId}")
    public void deleteRecipe(@PathVariable Long recipeId) {
        adminRecipeService.deleteById(recipeId);
    }
}
