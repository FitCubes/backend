package fitcubes.service;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    // ----- User methods -----

    ProductDto save(CreateProductDto createProductDto, Long userId);

    ProductDto getProductById(Long productId, Long userId);

    void deleteById(Long productId, Long userId);

    ProductDto update(UpdateProductDto updateProductDto, Long productId, Long userId);

    Page<ProductDto> getAllProducts(Pageable pageable, Long userId);

    // ----- Admin methods -----

    ProductDto getProductByIdAsAdmin(Long id);

    ProductDto saveAsAdmin(CreateProductDto createProductDto);

    void deleteByIdAsAdmin(Long productId);

    ProductDto updateAsAdmin(UpdateProductDto updateProductDto, Long productId);

    Page<ProductDto> getAllProductsAsAdmin(Pageable pageable);
}
