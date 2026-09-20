package fitcubes.mapper;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.model.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "caloriesPer100g", target = "calories")
    @Mapping(source = "proteinPer100g", target = "protein")
    @Mapping(source = "carbsPer100g", target = "carbohydrates")
    @Mapping(source = "fatsPer100g", target = "fat")
    Product toEntity(CreateProductDto createProductDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "caloriesPer100g", target = "calories")
    @Mapping(source = "proteinPer100g", target = "protein")
    @Mapping(source = "carbsPer100g", target = "carbohydrates")
    @Mapping(source = "fatsPer100g", target = "fat")
    void updateProduct(UpdateProductDto updateProductDto, @MappingTarget Product product);

    @Mapping(source = "calories", target = "caloriesPer100g")
    @Mapping(source = "protein", target = "proteinPer100g")
    @Mapping(source = "carbohydrates", target = "carbsPer100g")
    @Mapping(source = "fat", target = "fatsPer100g")
    ProductDto toDto(Product product);
}
