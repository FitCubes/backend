package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fitcubes.model.product.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FoodCalculationServiceTest {

    private final FoodCalculationService service = new FoodCalculationService();

    @Test
    void calculateCalories_multipliesByQuantity() {
        Product product = mock(Product.class);
        when(product.getCalories()).thenReturn(155.0);

        BigDecimal result = service.calculateCalories(product, BigDecimal.valueOf(2));

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(310.00));
    }

    @Test
    void calculateCalories_fractionalQuantity_roundsToTwoDecimals() {
        Product product = mock(Product.class);
        when(product.getCalories()).thenReturn(78.0);

        BigDecimal result = service.calculateCalories(product, BigDecimal.valueOf(0.5));

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(39.00));
    }

    @Test
    void calculateMacro_nullValue_returnsNull() {
        BigDecimal result = service.calculateMacro(null, BigDecimal.valueOf(2));

        assertThat(result).isNull();
    }

    @Test
    void calculateMacro_returnsScaledValue() {
        BigDecimal result = service.calculateMacro(BigDecimal.valueOf(6), BigDecimal.valueOf(2));

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(12.00));
    }
}