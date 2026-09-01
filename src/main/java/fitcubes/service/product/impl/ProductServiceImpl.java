package fitcubes.service.product.impl;

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
import fitcubes.service.product.ProductService;
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

        validateIsOwner(product, userId);

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductDto update(UpdateProductDto updateProductDto, Long productId, Long userId) {
        Product product = getProductByIdOrThrow(productId);

        validateIsOwner(product, userId);

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

        validateCanAccess(product, userId);

        return productMapper.toDto(product);
    }

    private void validateCanAccess(Product product, Long userId) {
        boolean isGlobal = product.getUser() == null;
        boolean isOwner = !isGlobal && product.getUser().getId().equals(userId);

        if (!isGlobal && !isOwner) {
            throw new AccessDeniedException("User with userId: " + userId
                    + " is not allowed to access product with productId: " + product.getId());
        }
    }

    private void validateIsOwner(Product product, Long userId) {
        if (product.getUser() == null || !product.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User with userId: " + userId
                    + " is not allowed to modify product with productId: " + product.getId());
        }
    }

    private Product getProductByIdOrThrow(Long productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException("Product with productId: "
                        + productId + " not found"));
    }
}
