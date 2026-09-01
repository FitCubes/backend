package fitcubes.controller;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import fitcubes.model.user.User;
import fitcubes.service.recipe.RecipeService;
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
@RequestMapping("/api/recipes")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@Tag(name = "User Recipe Management",
        description = "Endpoints for managing user-owned custom recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @Operation(
            summary = "Create user's custom recipe",
            description = "Creates a new user-owned custom recipe. "
                    + "Users can only create recipes for themselves.",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Recipe created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RecipeDto createRecipe(@RequestBody @Valid CreateRecipeDto createRecipeDto,
                                   @AuthenticationPrincipal User user) {
        return recipeService.save(createRecipeDto, user.getId());
    }

    @Operation(
            summary = "Get all recipes",
            description = "Retrieves a paginated list of recipes. "
                    + "Users can access both global recipes and their own custom recipes.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Recipes retrieved successfully")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<RecipeDto> getAllRecipes(@ParameterObject Pageable pageable,
                                         @RequestParam(required = false) String category,
                                         @AuthenticationPrincipal User user) {
        return recipeService.getAllRecipes(pageable, category, user.getId());
    }

    @Operation(
            summary = "Get recipe by ID",
            description = "Retrieves a recipe by its ID. "
                    + "Users can only access recipes created by themselves or global recipes.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Recipes retrieved successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: recipe belongs to another user"),
                    @ApiResponse(responseCode = "404",
                            description = "Recipe not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{recipeId}")
    public RecipeDto getRecipeById(@PathVariable Long recipeId,
                                     @AuthenticationPrincipal User user) {
        return recipeService.getRecipeById(recipeId, user.getId());
    }

    @Operation(
            summary = "Delete user's custom recipe",
            description = "Deletes a user-owned custom recipe by its ID. "
                    + "Users can only delete recipes created by themselves.",
            responses = {
                    @ApiResponse(responseCode = "204",
                            description = "Recipe deleted successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: recipe belongs to another user "
                                    + "or is global"),
                    @ApiResponse(responseCode = "404",
                            description = "Recipe not found")
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{recipeId}")
    public void deleteRecipe(@PathVariable Long recipeId, @AuthenticationPrincipal User user) {
        recipeService.deleteById(recipeId, user.getId());
    }

    @Operation(
            summary = "Update user's custom recipe",
            description = "Updates a user-owned custom recipe by its ID. "
                    + "Users can only modify recipes created by themselves.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Recipe updated successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: recipe belongs to another user "
                                    + "or is global"),
                    @ApiResponse(responseCode = "404",
                            description = "Recipe not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{recipeId}")
    public RecipeDto updateRecipe(@PathVariable Long recipeId,
                                  @AuthenticationPrincipal User user,
                                  @Valid @RequestBody UpdateRecipeDto updateRecipeDto) {
        return recipeService.update(updateRecipeDto, recipeId, user.getId());
    }
}
