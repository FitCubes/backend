package fitcubes.dto.product;

import fitcubes.model.product.ProductCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductDto(

        @NotBlank(message = "Name cannot be null")
        String name,

        @NotNull(message = "Category cannot be null")
        ProductCategory category,

        @Min(value = 0, message = "Calories must be greater than or to 0")
        Double caloriesPer100g,

        @Min(value = 0, message = "Fat must be greater than or to 0")
        Double fatsPer100g,

        @Min(value = 0, message = "Carbohydrates must be greater than or to 0")
        Double carbsPer100g,

        @Min(value = 0, message = "Protein must be greater than or to 0")
        Double proteinPer100g
) {
}
