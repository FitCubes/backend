package fitcubes.mapper;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.model.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(CreateProductDto createProductDto);

    ProductDto toDto(Product product);

    void updateProduct(UpdateProductDto updateProductDto, @MappingTarget Product product);
}
