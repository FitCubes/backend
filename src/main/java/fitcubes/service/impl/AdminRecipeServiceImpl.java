package fitcubes.service.impl;

import fitcubes.dto.recipe.CreateRecipeDto;
import fitcubes.dto.recipe.RecipeDto;
import fitcubes.dto.recipe.UpdateRecipeDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.RecipeMapper;
import fitcubes.model.recipe.Recipe;
import fitcubes.repository.RecipeRepository;
import fitcubes.service.AdminRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRecipeServiceImpl implements AdminRecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    @Override
    @Transactional
    public RecipeDto save(CreateRecipeDto createRecipeDto) {
        Recipe recipe = recipeMapper.toEntity(createRecipeDto);
        Recipe savedRecipe = recipeRepository.save(recipe);

        return recipeMapper.toDto(savedRecipe);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeDto getRecipeById(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
                () -> new EntityNotFoundException("Not found"));

        return recipeMapper.toDto(recipe);
    }

    @Override
    @Transactional
    public void deleteById(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
                () -> new EntityNotFoundException("Not found"));

        recipeRepository.delete(recipe);
    }

    @Override
    @Transactional
    public RecipeDto update(UpdateRecipeDto updateRecipeDto, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
                () -> new EntityNotFoundException("Not found"));

        recipeMapper.updateRecipeDto(updateRecipeDto, recipe);
        Recipe savedRecipe = recipeRepository.save(recipe);

        return recipeMapper.toDto(savedRecipe);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecipeDto> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable).map(recipeMapper::toDto);
    }
}
