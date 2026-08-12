package fitcubes.service;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.exception.AccessDeniedException;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.ProductMapper;
import fitcubes.model.product.Product;
import fitcubes.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto save(CreateProductDto createProductDto, Long userId) {
        Product product = productMapper.toEntity(createProductDto);
        product.setUserId(userId);
        Product savedProduct = productRepository.save(product);

        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public void deleteById(Long productId, Long userId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Product with productId: " + productId + " not found"));

        if (product.getUserId() == null || !product.getUserId().equals(userId)) {
            throw new AccessDeniedException("User with userId: " + userId
                    + " is not allowed to delete product with productId: " + productId);
        }

        productRepository.deleteById(productId);
    }

    @Override
    @Transactional
    public ProductDto update(UpdateProductDto updateProductDto, Long productId, Long userId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Product with productId: " + productId + " not found"));

        if (product.getUserId() == null || !product.getUserId().equals(userId)) {
            throw new AccessDeniedException("User with userId: " + userId
                    + " is not allowed to update product with productId: " + productId);
        }

        productMapper.updateProduct(updateProductDto, product);
        Product updatedProduct = productRepository.save(product);

        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId, Long userId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Product with productId: " + productId + " not found"));

        if (product.getUserId() != null && !product.getUserId().equals(userId)) {
            throw new AccessDeniedException("User with userId: " + userId
                    + " is not allowed to access product with productId: " + productId);
        }

        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public ProductDto saveAsAdmin(CreateProductDto createProductDto) {
        Product product = productMapper.toEntity(createProductDto);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductByIdAsAdmin(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException("Product with id: " + productId + " not found"));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public void deleteByIdAsAdmin(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product with productId: " + productId
                    + " not found");
        }

        productRepository.deleteById(productId);
    }

    @Override
    @Transactional
    public ProductDto updateAsAdmin(UpdateProductDto updateProductDto, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException("Product with productId: " + productId
                        + " not found"));

        productMapper.updateProduct(updateProductDto, product);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }
}
