package fitcubes.service;

import fitcubes.model.product.Product;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class FoodCalculationService {

    private static final int SCALE = 2;

    public BigDecimal calculateCalories(Product product, BigDecimal quantity) {
        return calculate(BigDecimal.valueOf(product.getCalories()), quantity);
    }

    public BigDecimal calculateMacro(BigDecimal macroValue, BigDecimal quantity) {
        if (macroValue == null) {
            return null;
        }
        return calculate(macroValue, quantity);
    }

    private BigDecimal calculate(BigDecimal baseValue, BigDecimal quantity) {
        return baseValue
                .multiply(quantity)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }
}
