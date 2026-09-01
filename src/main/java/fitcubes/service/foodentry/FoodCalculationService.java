package fitcubes.service.foodentry;

import fitcubes.model.product.Product;
import java.math.BigDecimal;

public interface FoodCalculationService {

    BigDecimal calculateCalories(Product product, BigDecimal quantity);

    BigDecimal calculateMacro(BigDecimal macroValue, BigDecimal quantity);

}
