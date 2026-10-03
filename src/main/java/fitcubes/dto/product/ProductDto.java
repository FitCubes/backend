package fitcubes.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import fitcubes.model.product.ProductCategory;

public record ProductDto(
        Long id,
        String name,
        ProductCategory category,
        Double caloriesPer100g,
        Double fatsPer100g,
        Double carbsPer100g,
        Double proteinPer100g,
        Double proteinCaloriesPer100g,
        @JsonProperty("isCustom") boolean isCustom
) {
    public ProductDto {
        if (proteinCaloriesPer100g == null) {
            proteinCaloriesPer100g = (proteinPer100g != null) ? proteinPer100g * 4.0 : 0.0;
        }
    }
}
