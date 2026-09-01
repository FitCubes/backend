package fitcubes.service;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.exception.AccessDeniedException;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.ProductMapper;
import fitcubes.model.product.Product;
import fitcubes.model.product.ProductCategory;
import fitcubes.model.user.User;
import fitcubes.repository.ProductRepository;
import fitcubes.repository.UserRepository;
import fitcubes.service.product.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(classes = ProductServiceImpl.class)
class ProductServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;
    private static final Long PRODUCT_ID = 10L;
    private static final String PRODUCT_NAME = "Grilled Chicken Breast";
    private static final ProductCategory CATEGORY = ProductCategory.values()[0];

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProductMapper productMapper;

    @Autowired
    private ProductServiceImpl productService;

    private User user;
    private Product product;
    private CreateProductDto createProductDto;
    private UpdateProductDto updateProductDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        user = createUser(USER_ID);
        product = createProduct(PRODUCT_ID, user);

        createProductDto = new CreateProductDto(PRODUCT_NAME, CATEGORY, 100, 2.0, 15.0, 5.0);
        updateProductDto = new UpdateProductDto(PRODUCT_NAME, CATEGORY, 120, 3.0, 18.0, 6.0);
        productDto = new ProductDto(PRODUCT_ID, PRODUCT_NAME, CATEGORY, 100, 2.0, 15.0, 5.0, 20.0);
    }

    private User createUser(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private Product createProduct(Long id, User owner) {
        Product p = new Product();
        p.setId(id);
        p.setUser(owner);
        p.setName(PRODUCT_NAME);
        p.setCategory(CATEGORY);
        p.setCalories(100.0);
        p.setFat(2.0);
        p.setProtein(5.0);
        p.setCarbohydrates(15.0);
        return p;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("save_userExists_savesProductWithUser")
        void save_userExists_savesProductWithUser() {
            Product mappedProduct = createProduct(null, null); // toEntity ignoruje id i user
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(productMapper.toEntity(createProductDto)).willReturn(mappedProduct);
            given(productRepository.save(mappedProduct)).willReturn(product);
            given(productMapper.toDto(product)).willReturn(productDto);

            ProductDto result = productService.save(createProductDto, USER_ID);

            assertThat(result).isEqualTo(productDto);
            assertThat(mappedProduct.getUser()).isEqualTo(user);
            verify(productRepository).save(mappedProduct);
        }

        @Test
        @DisplayName("save_userNotFound_throwsEntityNotFoundException")
        void save_userNotFound_throwsEntityNotFoundException() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.save(createProductDto, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(String.valueOf(USER_ID));

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("deleteById_ownerMatches_deletesProduct")
        void deleteById_ownerMatches_deletesProduct() {
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

            productService.deleteById(PRODUCT_ID, USER_ID);

            verify(productRepository).delete(product);
        }

        @Test
        @DisplayName("deleteById_productNotFound_throwsEntityNotFoundException")
        void deleteById_productNotFound_throwsEntityNotFoundException() {
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteById(PRODUCT_ID, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(productRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("deleteById_notOwner_throwsAccessDeniedException")
        void deleteById_notOwner_throwsAccessDeniedException() {
            Product foreignProduct = createProduct(PRODUCT_ID, createUser(OTHER_USER_ID));
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(foreignProduct));

            assertThatThrownBy(() -> productService.deleteById(PRODUCT_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(productRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("deleteById_globalProduct_throwsAccessDeniedException")
        void deleteById_globalProduct_throwsAccessDeniedException() {
            Product globalProduct = createProduct(PRODUCT_ID, null);
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(globalProduct));

            assertThatThrownBy(() -> productService.deleteById(PRODUCT_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(productRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("update_ownerMatches_updatesProduct")
        void update_ownerMatches_updatesProduct() {
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toDto(product)).willReturn(productDto);

            ProductDto result = productService.update(updateProductDto, PRODUCT_ID, USER_ID);

            assertThat(result).isEqualTo(productDto);
            verify(productMapper).updateProduct(updateProductDto, product);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("update_notOwner_throwsAccessDeniedException")
        void update_notOwner_throwsAccessDeniedException() {
            Product foreignProduct = createProduct(PRODUCT_ID, createUser(OTHER_USER_ID));
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(foreignProduct));

            assertThatThrownBy(() -> productService.update(updateProductDto, PRODUCT_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(productMapper, never()).updateProduct(any(), any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("update_globalProduct_throwsAccessDeniedException")
        void update_globalProduct_throwsAccessDeniedException() {
            Product globalProduct = createProduct(PRODUCT_ID, null);
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(globalProduct));

            assertThatThrownBy(() -> productService.update(updateProductDto, PRODUCT_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(productMapper, never()).updateProduct(any(), any());
        }

        @Test
        @DisplayName("update_productNotFound_throwsEntityNotFoundException")
        void update_productNotFound_throwsEntityNotFoundException() {
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(updateProductDto, PRODUCT_ID, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("getAllProducts_returnsMappedPage")
        void getAllProducts_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(product));
            given(productRepository.findAllGlobalOrByUserId(USER_ID, pageable)).willReturn(productPage);
            given(productMapper.toDto(product)).willReturn(productDto);

            Page<ProductDto> result = productService.getAllProducts(pageable, USER_ID);

            assertThat(result.getContent()).containsExactly(productDto);
        }

        @Test
        @DisplayName("getAllProducts_noProducts_returnsEmptyPage")
        void getAllProducts_noProducts_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            given(productRepository.findAllGlobalOrByUserId(USER_ID, pageable))
                    .willReturn(Page.empty(pageable));

            Page<ProductDto> result = productService.getAllProducts(pageable, USER_ID);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("getProductById_ownerMatches_returnsDto")
        void getProductById_ownerMatches_returnsDto() {
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productMapper.toDto(product)).willReturn(productDto);

            ProductDto result = productService.getProductById(PRODUCT_ID, USER_ID);

            assertThat(result).isEqualTo(productDto);
        }

        @Test
        @DisplayName("getProductById_globalProduct_returnsDto")
        void getProductById_globalProduct_returnsDto() {
            Product globalProduct = createProduct(PRODUCT_ID, null);
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(globalProduct));
            given(productMapper.toDto(globalProduct)).willReturn(productDto);

            ProductDto result = productService.getProductById(PRODUCT_ID, USER_ID);

            assertThat(result).isEqualTo(productDto);
        }

        @Test
        @DisplayName("getProductById_notOwner_throwsAccessDeniedException")
        void getProductById_notOwner_throwsAccessDeniedException() {
            Product foreignProduct = createProduct(PRODUCT_ID, createUser(OTHER_USER_ID));
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(foreignProduct));

            assertThatThrownBy(() -> productService.getProductById(PRODUCT_ID, USER_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("getProductById_productNotFound_throwsEntityNotFoundException")
        void getProductById_productNotFound_throwsEntityNotFoundException() {
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(PRODUCT_ID, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

}