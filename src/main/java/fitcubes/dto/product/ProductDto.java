package fitcubes.dto.product;

import fitcubes.model.product.ProductCategory;

public record ProductDto(
        Long id,
        String name,
        ProductCategory category,
        Integer calories,
        Double fat,
        Double carbohydrates,
        Double protein,
        Double proteinCalories
) {
    public ProductDto {
        if (proteinCalories == null) {
            proteinCalories = (protein != null) ? protein * 4.0 : 0.0;
        }
    }
}
