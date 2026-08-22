package fitcubes.dto.product;

import fitcubes.model.product.ProductCategory;

public record UpdateProductDto(
        String name,
        ProductCategory category,
        Integer calories,
        Double fat,
        Double carbohydrates,
        Double protein
) {
}
