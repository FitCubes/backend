package fitcubes.dto.product;

import fitcubes.model.product.ProductCategory;

public record UpdateProductDto(
        String name,
        ProductCategory category,
        Double caloriesPer100g,
        Double fatsPer100g,
        Double carbsPer100g,
        Double proteinPer100g
) {
}
