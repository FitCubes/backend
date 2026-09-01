package fitcubes.service;

import fitcubes.model.recipe.Recipe;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class RecipeCalculationService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int SCALE = 2;

    public void recalculate(Recipe recipe) {
        recipe.setCaloriesPerServing(perServing(recipe.getCaloriesPer100g(), recipe));
        recipe.setProteinPerServing(perServing(recipe.getProteinPer100g(), recipe));
        recipe.setCarbsPerServing(perServing(recipe.getCarbsPer100g(), recipe));
        recipe.setFatsPerServing(perServing(recipe.getFatsPer100g(), recipe));
    }

    private BigDecimal perServing(BigDecimal per100gValue, Recipe recipe) {
        if (per100gValue == null) {
            return null;
        }
        BigDecimal totalValue = per100gValue
                .multiply(recipe.getCookedWeight())
                .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

        return totalValue.divide(
                BigDecimal.valueOf(recipe.getServings()), SCALE, RoundingMode.HALF_UP);
    }
}
