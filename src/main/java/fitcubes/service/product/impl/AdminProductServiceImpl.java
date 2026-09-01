package fitcubes.service.product.impl;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.ProductMapper;
import fitcubes.model.product.Product;
import fitcubes.repository.ProductRepository;
import fitcubes.service.product.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto save(CreateProductDto createProductDto) {
        Product product = productMapper.toEntity(createProductDto);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId) {
        Product product = getProductByIdOrThrow(productId);
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public void deleteById(Long productId) {
        Product product = getProductByIdOrThrow(productId);

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductDto update(UpdateProductDto updateProductDto, Long productId) {
        Product product = getProductByIdOrThrow(productId);

        productMapper.updateProduct(updateProductDto, product);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    private Product getProductByIdOrThrow(Long productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException("Product with productId: "
                        + productId + " not found"));
    }
}
