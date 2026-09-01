package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;

import fitcubes.model.recipe.Recipe;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RecipeCalculationServiceTest {

    private final RecipeCalculationService service = new RecipeCalculationService();

    @Test
    void recalculate_computesCaloriesPerServing() {
        Recipe recipe = new Recipe();
        recipe.setCaloriesPer100g(BigDecimal.valueOf(250));
        recipe.setProteinPer100g(BigDecimal.valueOf(20));
        recipe.setCarbsPer100g(BigDecimal.valueOf(30));
        recipe.setFatsPer100g(BigDecimal.valueOf(10));
        recipe.setCookedWeight(BigDecimal.valueOf(800));
        recipe.setServings(4);

        service.recalculate(recipe);

        assertThat(recipe.getCaloriesPerServing()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(recipe.getProteinPerServing()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
        assertThat(recipe.getCarbsPerServing()).isEqualByComparingTo(BigDecimal.valueOf(60.00));
        assertThat(recipe.getFatsPerServing()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
    }

    @Test
    void recalculate_nullMacro_staysNull() {
        Recipe recipe = new Recipe();
        recipe.setCaloriesPer100g(BigDecimal.valueOf(250));
        recipe.setProteinPer100g(null);
        recipe.setCarbsPer100g(BigDecimal.valueOf(30));
        recipe.setFatsPer100g(BigDecimal.valueOf(10));
        recipe.setCookedWeight(BigDecimal.valueOf(800));
        recipe.setServings(4);

        service.recalculate(recipe);

        assertThat(recipe.getProteinPerServing()).isNull();
        assertThat(recipe.getCaloriesPerServing()).isNotNull();
    }
}