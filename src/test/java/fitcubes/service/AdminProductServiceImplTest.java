package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.mapper.ProductMapper;
import fitcubes.model.product.Product;
import fitcubes.model.product.ProductCategory;
import fitcubes.model.user.User;
import fitcubes.repository.ProductRepository;
import fitcubes.repository.UserRepository;
import fitcubes.service.impl.AdminProductServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = AdminProductServiceImpl.class)
public class AdminProductServiceImplTest {

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
    private AdminProductServiceImpl adminProductService;

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
        p.setCalories(100);
        p.setFat(2.0);
        p.setProtein(5.0);
        p.setCarbohydrates(15.0);
        return p;
    }

    @Test
    @DisplayName("save_savesProductWithoutOwnerCheck")
    void save_savesProductWithoutOwnerCheck() {
        Product mappedProduct = createProduct(null, null);
        given(productMapper.toEntity(createProductDto)).willReturn(mappedProduct);
        given(productRepository.save(mappedProduct)).willReturn(product);
        given(productMapper.toDto(product)).willReturn(productDto);

        ProductDto result = adminProductService.save(createProductDto);

        assertThat(result).isEqualTo(productDto);
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getProductById_returnsDtoRegardlessOfOwner")
    void getProductById_returnsDtoRegardlessOfOwner() {
        Product foreignProduct = createProduct(PRODUCT_ID, createUser(OTHER_USER_ID));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(foreignProduct));
        given(productMapper.toDto(foreignProduct)).willReturn(productDto);

        ProductDto result = adminProductService.getProductById(PRODUCT_ID);

        assertThat(result).isEqualTo(productDto);
    }

    @Test
    @DisplayName("getProductById_notFound_throwsEntityNotFoundException")
    void getProductById_notFound_throwsEntityNotFoundException() {
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.getProductById(PRODUCT_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("deleteById_deletesProductRegardlessOfOwner")
    void deleteById_deletesProductRegardlessOfOwner() {
        Product foreignProduct = createProduct(PRODUCT_ID, createUser(OTHER_USER_ID));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(foreignProduct));

        adminProductService.deleteById(PRODUCT_ID);

        verify(productRepository).delete(foreignProduct);
    }

    @Test
    @DisplayName("deleteById_notFound_throwsEntityNotFoundException")
    void deleteById_notFound_throwsEntityNotFoundException() {
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.deleteById(PRODUCT_ID))
                .isInstanceOf(EntityNotFoundException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("update_updatesProductRegardlessOfOwner")
    void update_updatesProductRegardlessOfOwner() {
        Product foreignProduct = createProduct(PRODUCT_ID, createUser(OTHER_USER_ID));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(foreignProduct));
        given(productRepository.save(foreignProduct)).willReturn(foreignProduct);
        given(productMapper.toDto(foreignProduct)).willReturn(productDto);

        ProductDto result = adminProductService.update(updateProductDto, PRODUCT_ID);

        assertThat(result).isEqualTo(productDto);
        verify(productMapper).updateProduct(updateProductDto, foreignProduct);
    }

    @Test
    @DisplayName("getAllProduct_returnsAllProducts")
    void getAllProducts_returnsAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));
        given(productRepository.findAll(pageable)).willReturn(productPage);
        given(productMapper.toDto(product)).willReturn(productDto);

        Page<ProductDto> result = adminProductService.getAllProducts(pageable);

        assertThat(result.getContent()).containsExactly(productDto);
    }
}
