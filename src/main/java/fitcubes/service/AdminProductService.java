package fitcubes.service;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminProductService {

    ProductDto getProductById(Long id);

    ProductDto save(CreateProductDto createProductDto);

    void deleteById(Long productId);

    ProductDto update(UpdateProductDto updateProductDto, Long productId);

    Page<ProductDto> getAllProducts(Pageable pageable);
}
