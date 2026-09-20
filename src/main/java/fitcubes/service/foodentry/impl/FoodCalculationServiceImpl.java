package fitcubes.service.foodentry.impl;

import fitcubes.model.product.Product;
import fitcubes.service.foodentry.FoodCalculationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class FoodCalculationServiceImpl implements FoodCalculationService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int SCALE = 2;

    @Override
    public BigDecimal calculateCalories(Product product, BigDecimal quantity) {
        return calculate(BigDecimal.valueOf(product.getCalories()), quantity);
    }

    @Override
    public BigDecimal calculateMacro(BigDecimal macroValue, BigDecimal quantity) {
        if (macroValue == null) {
            return null;
        }
        return calculate(macroValue, quantity);
    }

    private BigDecimal calculate(BigDecimal baseValue, BigDecimal quantity) {
        return baseValue
                .multiply(quantity)
                .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
    }
}
