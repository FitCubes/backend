package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fitcubes.dto.foodentry.FoodEntryRequestDto;
import fitcubes.dto.foodentry.FoodEntryResponseDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.FoodEntryMapper;
import fitcubes.model.foodentry.FoodEntry;
import fitcubes.model.foodentry.MealType;
import fitcubes.model.foodentry.SourceType;
import fitcubes.model.product.Product;
import fitcubes.model.recipe.Recipe;
import fitcubes.repository.FoodEntryRepository;
import fitcubes.repository.ProductRepository;
import fitcubes.repository.RecipeRepository;
import fitcubes.service.foodentry.FoodCalculationService;
import fitcubes.service.foodentry.impl.FoodEntryServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FoodEntryServiceImplTest {

    @Mock
    private FoodEntryRepository foodEntryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private FoodEntryMapper foodEntryMapper;

    @Mock
    private FoodCalculationService calculationService;

    @InjectMocks
    private FoodEntryServiceImpl service;

    private static final Long USER_ID = 42L;
    private static final Long ENTRY_ID = 10L;

    private FoodEntry newFoodEntry() {
        return new FoodEntry();
    }

    private FoodEntryRequestDto productRequest() {
        return new FoodEntryRequestDto(
                SourceType.PRODUCT, 1L, null, null, null,
                null, null, null, null,
                BigDecimal.valueOf(2), MealType.BREAKFAST, Instant.now()
        );
    }

    private FoodEntryRequestDto recipeRequest() {
        return new FoodEntryRequestDto(
                SourceType.RECIPE, null, 5L, null, null,
                null, null, null, null,
                BigDecimal.valueOf(1), MealType.LUNCH, Instant.now()
        );
    }

    private FoodEntryRequestDto customRequest() {
        return new FoodEntryRequestDto(
                SourceType.CUSTOM, null, null, "Homemade soup", null,
                BigDecimal.valueOf(350), BigDecimal.valueOf(12), BigDecimal.valueOf(40), BigDecimal.valueOf(15),
                BigDecimal.valueOf(1), MealType.DINNER, Instant.now()
        );
    }

    @Test
    void addFoodEntry_withProduct_savesAndReturnsDto() {
        FoodEntryRequestDto request = productRequest();
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Egg");

        FoodEntry mappedEntity = newFoodEntry();
        FoodEntry savedEntity = newFoodEntry();
        FoodEntryResponseDto expectedDto = mock(FoodEntryResponseDto.class);

        when(foodEntryMapper.toEntity(request)).thenReturn(mappedEntity);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(calculationService.calculateCalories(product, request.quantity()))
                .thenReturn(BigDecimal.valueOf(156));
        when(calculationService.calculateMacro(any(), any())).thenReturn(BigDecimal.TEN);
        when(foodEntryRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(foodEntryMapper.toDto(savedEntity)).thenReturn(expectedDto);

        FoodEntryResponseDto result = service.addFoodEntry(USER_ID, request);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(mappedEntity.getUserId()).isEqualTo(USER_ID);
        assertThat(mappedEntity.getProductId()).isEqualTo(1L);
        assertThat(mappedEntity.getRecipeId()).isNull();
        assertThat(mappedEntity.getNameSnapshot()).isEqualTo("Egg");
        assertThat(mappedEntity.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(156));
        verify(foodEntryRepository).save(mappedEntity);
    }

    @Test
    void addFoodEntry_productNotFound_throwsEntityNotFound() {
        FoodEntryRequestDto request = productRequest();
        when(foodEntryMapper.toEntity(request)).thenReturn(newFoodEntry());
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addFoodEntry(USER_ID, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(foodEntryRepository, never()).save(any());
    }

    @Test
    void addFoodEntry_missingProductId_throwsIllegalArgument() {
        FoodEntryRequestDto invalid = new FoodEntryRequestDto(
                SourceType.PRODUCT, null, null, null, null,
                null, null, null, null,
                BigDecimal.valueOf(2), MealType.BREAKFAST, Instant.now()
        );

        assertThatThrownBy(() -> service.addFoodEntry(USER_ID, invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId");

        verify(foodEntryRepository, never()).save(any());
    }

    @Test
    void addFoodEntry_withRecipe_savesAndReturnsDto() {
        FoodEntryRequestDto request = recipeRequest();
        Recipe recipe = new Recipe();
        recipe.setId(5L);
        recipe.setName("Chicken soup");
        recipe.setCaloriesPerServing(BigDecimal.valueOf(500));
        recipe.setProteinPerServing(BigDecimal.valueOf(40));
        recipe.setCarbsPerServing(BigDecimal.valueOf(60));
        recipe.setFatsPerServing(BigDecimal.valueOf(20));

        FoodEntry mappedEntity = newFoodEntry();
        FoodEntry savedEntity = newFoodEntry();
        FoodEntryResponseDto expectedDto = mock(FoodEntryResponseDto.class);

        when(foodEntryMapper.toEntity(request)).thenReturn(mappedEntity);
        when(recipeRepository.findById(5L)).thenReturn(Optional.of(recipe));
        when(foodEntryRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(foodEntryMapper.toDto(savedEntity)).thenReturn(expectedDto);

        FoodEntryResponseDto result = service.addFoodEntry(USER_ID, request);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(mappedEntity.getRecipeId()).isEqualTo(5L);
        assertThat(mappedEntity.getProductId()).isNull();
        assertThat(mappedEntity.getNameSnapshot()).isEqualTo("Chicken soup");
        assertThat(mappedEntity.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void addFoodEntry_missingRecipeId_throwsIllegalArgument() {
        FoodEntryRequestDto invalid = new FoodEntryRequestDto(
                SourceType.RECIPE, null, null, null, null,
                null, null, null, null,
                BigDecimal.valueOf(1), MealType.LUNCH, Instant.now()
        );

        assertThatThrownBy(() -> service.addFoodEntry(USER_ID, invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("recipeId");
    }

    @Test
    void addFoodEntry_withCustom_savesAndReturnsDto() {
        FoodEntryRequestDto request = customRequest();
        FoodEntry mappedEntity = newFoodEntry();
        FoodEntry savedEntity = newFoodEntry();
        FoodEntryResponseDto expectedDto = mock(FoodEntryResponseDto.class);

        when(foodEntryMapper.toEntity(request)).thenReturn(mappedEntity);
        when(foodEntryRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(foodEntryMapper.toDto(savedEntity)).thenReturn(expectedDto);

        FoodEntryResponseDto result = service.addFoodEntry(USER_ID, request);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(mappedEntity.getNameSnapshot()).isEqualTo("Homemade soup");
        assertThat(mappedEntity.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(350));
        verify(productRepository, never()).findById(any());
        verify(recipeRepository, never()).findById(any());
    }

    @Test
    void addFoodEntry_customMissingName_throwsIllegalArgument() {
        FoodEntryRequestDto invalid = new FoodEntryRequestDto(
                SourceType.CUSTOM, null, null, null, null,
                BigDecimal.valueOf(350), null, null, null,
                BigDecimal.ONE, MealType.DINNER, Instant.now()
        );

        assertThatThrownBy(() -> service.addFoodEntry(USER_ID, invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customName");
    }

    @Test
    void addFoodEntry_customMissingCalories_throwsIllegalArgument() {
        FoodEntryRequestDto invalid = new FoodEntryRequestDto(
                SourceType.CUSTOM, null, null, "Soup", null,
                null, null, null, null,
                BigDecimal.ONE, MealType.DINNER, Instant.now()
        );

        assertThatThrownBy(() -> service.addFoodEntry(USER_ID, invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customCalories");
    }

    @Test
    void updateFoodEntry_notFound_throwsEntityNotFound() {
        FoodEntryRequestDto request = productRequest();
        when(foodEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateFoodEntry(USER_ID, ENTRY_ID, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(foodEntryRepository, never()).save(any());
    }

    @Test
    void updateFoodEntry_existingEntry_updatesAndReturnsDto() {
        FoodEntryRequestDto request = productRequest();
        FoodEntry existingEntry = newFoodEntry();
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Egg");
        FoodEntryResponseDto expectedDto = mock(FoodEntryResponseDto.class);

        when(foodEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(Optional.of(existingEntry));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(calculationService.calculateCalories(any(), any())).thenReturn(BigDecimal.valueOf(200));
        when(calculationService.calculateMacro(any(), any())).thenReturn(BigDecimal.ONE);
        when(foodEntryRepository.save(existingEntry)).thenReturn(existingEntry);
        when(foodEntryMapper.toDto(existingEntry)).thenReturn(expectedDto);

        FoodEntryResponseDto result = service.updateFoodEntry(USER_ID, ENTRY_ID, request);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(existingEntry.getQuantity()).isEqualByComparingTo(request.quantity());
        assertThat(existingEntry.getMealType()).isEqualTo(request.mealType());
        verify(foodEntryMapper, never()).toEntity(any());
    }

    @Test
    void deleteFoodEntry_existingEntry_deletesIt() {
        FoodEntry entry = newFoodEntry();
        when(foodEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(Optional.of(entry));

        service.deleteFoodEntry(USER_ID, ENTRY_ID);

        verify(foodEntryRepository, times(1)).delete(entry);
    }

    @Test
    void deleteFoodEntry_notFound_throwsEntityNotFound() {
        when(foodEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteFoodEntry(USER_ID, ENTRY_ID))
                .isInstanceOf(EntityNotFoundException.class);

        verify(foodEntryRepository, never()).delete(any());
    }

    @Test
    void getFoodEntry_existingEntry_returnsDto() {
        FoodEntry entry = newFoodEntry();
        FoodEntryResponseDto expectedDto = mock(FoodEntryResponseDto.class);

        when(foodEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(Optional.of(entry));
        when(foodEntryMapper.toDto(entry)).thenReturn(expectedDto);

        FoodEntryResponseDto result = service.getFoodEntry(USER_ID, ENTRY_ID);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void getFoodEntry_notFound_throwsEntityNotFound() {
        when(foodEntryRepository.findByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFoodEntry(USER_ID, ENTRY_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getFoodEntries_returnsMappedPage() {
        Instant from = Instant.parse("2026-08-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-31T23:59:59Z");
        Pageable pageable = PageRequest.of(0, 20);

        FoodEntry entry = newFoodEntry();
        FoodEntryResponseDto dto = mock(FoodEntryResponseDto.class);
        Page<FoodEntry> entityPage = new PageImpl<>(List.of(entry), pageable, 1);

        when(foodEntryRepository.findByUserIdAndLoggedAtBetween(USER_ID, from, to, pageable))
                .thenReturn(entityPage);
        when(foodEntryMapper.toDto(entry)).thenReturn(dto);

        Page<FoodEntryResponseDto> result = service.getFoodEntries(USER_ID, from, to, pageable);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}