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
        Integer calories,

        @Min(value = 0, message = "Fat must be greater than or to 0")
        Double fat,

        @Min(value = 0, message = "Carbohydrates must be greater than or to 0")
        Double carbohydrates,

        @Min(value = 0, message = "Protein must be greater than or to 0")
        Double protein
) {
}
