package fitcubes.service;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.exception.AccessDeniedException;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.ProductMapper;
import fitcubes.model.product.Product;
import fitcubes.model.user.User;
import fitcubes.repository.ProductRepository;
import fitcubes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto save(CreateProductDto createProductDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(
                        "User with userId: " + userId + " not found"));

        Product product = productMapper.toEntity(createProductDto);
        product.setUser(user);
        Product savedProduct = productRepository.save(product);

        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public void deleteById(Long productId, Long userId) {
        Product product = getProductByIdOrThrow(productId);

        validateUserOwnership(product, userId, "delete");

        productRepository.deleteById(productId);
    }

    @Override
    @Transactional
    public ProductDto update(UpdateProductDto updateProductDto, Long productId, Long userId) {
        Product product = getProductByIdOrThrow(productId);

        validateUserOwnership(product, userId, "update");

        productMapper.updateProduct(updateProductDto, product);
        Product updatedProduct = productRepository.save(product);

        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable, Long userId) {
        return productRepository.findAllGlobalOrByUserId(userId, pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId, Long userId) {
        Product product = getProductByIdOrThrow(productId);

        if (product.getUser() != null && !product.getUser().getId().equals(userId)) {
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
        Product product = getProductByIdOrThrow(productId);
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public void deleteByIdAsAdmin(Long productId) {
        Product product = getProductByIdOrThrow(productId);

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductDto updateAsAdmin(UpdateProductDto updateProductDto, Long productId) {
        Product product = getProductByIdOrThrow(productId);

        productMapper.updateProduct(updateProductDto, product);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProductsAsAdmin(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    private void validateUserOwnership(Product product, Long userId, String operation) {
        if (product.getUser() == null || !product.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User with userId: " + userId
                    + " is not allowed to " + operation
                    + " product with productId: " + product.getId());
        }
    }

    private Product getProductByIdOrThrow(Long productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException("Product with productId: "
                        + productId + " not found"));
    }
}
