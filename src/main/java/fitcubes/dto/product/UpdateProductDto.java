package fitcubes.dto.product;

import fitcubes.model.product.ProductCategory;

public record UpdateProductDto(
        String name,
        ProductCategory category,
        int calories,
        double fat,
        double carbohydrates,
        double protein
) {
}
