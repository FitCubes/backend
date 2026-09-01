package fitcubes.service.foodentry.impl;

import fitcubes.dto.foodentry.FoodEntryRequestDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.exception.UnsupportedSourceTypeException;
import fitcubes.mapper.FoodEntryMapper;
import fitcubes.model.foodentry.FoodEntry;
import fitcubes.model.product.Product;
import fitcubes.model.recipe.Recipe;
import fitcubes.repository.FoodEntryRepository;
import fitcubes.repository.ProductRepository;
import fitcubes.repository.RecipeRepository;
import fitcubes.service.foodentry.FoodCalculationService;
import fitcubes.service.foodentry.FoodEntryService;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FoodEntryServiceImpl implements FoodEntryService {

    private final FoodEntryRepository foodEntryRepository;
    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;
    private final FoodEntryMapper foodEntryMapper;
    private final FoodCalculationService calculationService;

    @Override
    @Transactional
    public FoodEntryResponseDto addFoodEntry(Long userId, FoodEntryRequestDto requestDto) {
        validateRequest(requestDto);

        FoodEntry entry = foodEntryMapper.toEntity(requestDto);
        entry.setUserId(userId);

        applySource(entry, requestDto);

        FoodEntry saved = foodEntryRepository.save(entry);
        return foodEntryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public FoodEntryResponseDto updateFoodEntry(Long userId, Long entryId,
                                                FoodEntryRequestDto requestDto) {
        validateRequest(requestDto);

        FoodEntry entry = foodEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Food entry not found: " + entryId));

        entry.setSourceType(requestDto.sourceType());
        entry.setQuantity(requestDto.quantity());
        entry.setMealType(requestDto.mealType());
        entry.setLoggedAt(requestDto.loggedAt());

        applySource(entry, requestDto);

        FoodEntry saved = foodEntryRepository.save(entry);
        return foodEntryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteFoodEntry(Long userId, Long entryId) {
        FoodEntry entry = foodEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Food entry not found: " + entryId));

        foodEntryRepository.delete(entry);
    }

    @Override
    public FoodEntryResponseDto getFoodEntry(Long userId, Long entryId) {
        FoodEntry entry = foodEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Food entry not found: " + entryId));

        return foodEntryMapper.toDto(entry);
    }

    @Override
    public Page<FoodEntryResponseDto> getFoodEntries(Long userId, Instant from, Instant to,
                                                     Pageable pageable) {
        return foodEntryRepository
                .findByUserIdAndLoggedAtBetween(userId, from, to, pageable)
                .map(foodEntryMapper::toDto);
    }

    private void validateRequest(FoodEntryRequestDto requestDto) {
        switch (requestDto.sourceType()) {
            case PRODUCT -> {
                if (requestDto.productId() == null) {
                    throw new IllegalArgumentException(
                            "productId is required for source_type=PRODUCT");
                }
            }
            case RECIPE -> {
                if (requestDto.recipeId() == null) {
                    throw new IllegalArgumentException(
                            "recipeId is required for source_type=RECIPE");
                }
            }
            case CUSTOM -> {
                if (requestDto.customName() == null || requestDto.customName().isBlank()) {
                    throw new IllegalArgumentException(
                            "customName is required for source_type=CUSTOM");
                }
                if (requestDto.customCalories() == null) {
                    throw new IllegalArgumentException(
                            "customCalories is required for source_type=CUSTOM");
                }
            }
            default -> throw new UnsupportedSourceTypeException(
                    "Unsupported source_type: " + requestDto.sourceType());
        }
    }

    private void applySource(FoodEntry entry, FoodEntryRequestDto requestDto) {
        switch (requestDto.sourceType()) {
            case PRODUCT -> applyProduct(entry, requestDto);
            case RECIPE -> applyRecipe(entry, requestDto);
            case CUSTOM -> applyCustom(entry, requestDto);
            default -> throw new UnsupportedSourceTypeException(
                    "Unsupported source_type: " + requestDto.sourceType());
        }
    }

    private void applyProduct(FoodEntry entry, FoodEntryRequestDto requestDto) {
        Product product = productRepository.findById(requestDto.productId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found: " + requestDto.productId()));

        entry.setProductId(product.getId());
        entry.setRecipeId(null);
        entry.setNameSnapshot(product.getName());
        entry.setCalories(calculationService.calculateCalories(product, requestDto.quantity()));
        entry.setProtein(calculationService.calculateMacro(
                BigDecimal.valueOf(product.getProtein()), requestDto.quantity()));
        entry.setCarbs(calculationService.calculateMacro(
                BigDecimal.valueOf(product.getCarbohydrates()), requestDto.quantity()));
        entry.setFat(calculationService.calculateMacro(
                BigDecimal.valueOf(product.getFat()), requestDto.quantity()));
    }

    private void applyRecipe(FoodEntry entry, FoodEntryRequestDto requestDto) {
        Recipe recipe = recipeRepository.findById(requestDto.recipeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recipe not found: " + requestDto.recipeId()));

        entry.setRecipeId(recipe.getId());
        entry.setProductId(null);
        entry.setNameSnapshot(recipe.getName());
        entry.setCalories(recipe.getCaloriesPerServing().multiply(requestDto.quantity()));
        entry.setProtein(recipe.getProteinPerServing() == null ? null
                : recipe.getProteinPerServing().multiply(requestDto.quantity()));
        entry.setCarbs(recipe.getCarbsPerServing() == null ? null
                : recipe.getCarbsPerServing().multiply(requestDto.quantity()));
        entry.setFat(recipe.getFatsPerServing() == null ? null
                : recipe.getFatsPerServing().multiply(requestDto.quantity()));
    }

    private void applyCustom(FoodEntry entry, FoodEntryRequestDto requestDto) {
        entry.setProductId(null);
        entry.setRecipeId(null);
        entry.setNameSnapshot(requestDto.customName());
        entry.setCalories(requestDto.customCalories());
        entry.setProtein(requestDto.customProtein());
        entry.setCarbs(requestDto.customCarbs());
        entry.setFat(requestDto.customFat());
    }
}
