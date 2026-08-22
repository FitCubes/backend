package fitcubes.service.impl;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import fitcubes.exception.AccessDeniedException;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.RecipeMapper;
import fitcubes.model.recipe.Recipe;
import fitcubes.model.user.User;
import fitcubes.repository.RecipeRepository;
import fitcubes.repository.UserRepository;
import fitcubes.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;

    @Override
    @Transactional
    public RecipeDto save(CreateRecipeDto createRecipeDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(
                        "User with userId: " + userId + " not found"));

        Recipe recipe = recipeMapper.toEntity(createRecipeDto);
        recipe.setUser(user);
        Recipe savedRecipe = recipeRepository.save(recipe);

        return recipeMapper.toDto(savedRecipe);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeDto getRecipeById(Long recipeId, Long userId) {
        Recipe recipe = getRecipeByIdOrThrow(recipeId);

        validateCanAccess(recipe, userId);

        return recipeMapper.toDto(recipe);
    }

    @Override
    @Transactional
    public void deleteById(Long recipeId, Long userId) {
        Recipe recipe = getRecipeByIdOrThrow(recipeId);

        validateIsOwner(recipe, userId);

        recipeRepository.delete(recipe);
    }

    @Override
    @Transactional
    public RecipeDto update(UpdateRecipeDto updateRecipeDto, Long recipeId, Long userId) {
        Recipe recipe = getRecipeByIdOrThrow(recipeId);

        validateIsOwner(recipe, userId);

        recipeMapper.updateRecipeDto(updateRecipeDto, recipe);
        Recipe updatedRecipe = recipeRepository.save(recipe);

        return recipeMapper.toDto(updatedRecipe);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecipeDto> getAllRecipes(Pageable pageable, String category, Long userId) {
        return recipeRepository.findAllGlobalOrByUserIdAndCategory(userId, category, pageable)
                .map(recipeMapper::toDto);
    }

    private Recipe getRecipeByIdOrThrow(Long recipeId) {
        return recipeRepository.findById(recipeId).orElseThrow(
                () -> new EntityNotFoundException("Recipe with recipeId: "
                        + recipeId + " not found"));
    }

    private void validateCanAccess(Recipe recipe, Long userId) {
        boolean isGlobal = recipe.getUser() == null;
        boolean isOwner = !isGlobal && recipe.getUser().getId().equals(userId);

        if (!isGlobal && !isOwner) {
            throw new AccessDeniedException("User with userId: " + userId
                    + " is not allowed to access recipe with recipeId: " + recipe.getId());
        }
    }

    private void validateIsOwner(Recipe recipe, Long userId) {
        if (recipe.getUser() == null || !recipe.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User with userId: " + userId
                    + " is not allowed to modify recipe with recipeId: " + recipe.getId());
        }
    }
}
